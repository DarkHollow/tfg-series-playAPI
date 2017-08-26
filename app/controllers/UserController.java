package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import models.service.UserService;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.Security.*;

import javax.inject.Inject;

public class UserController extends Controller {

  private final UserService userService;
  private final FormFactory formFactory;
  private final utils.Security.Administrator adminAuth;
  private final utils.Security.User userAuth;

  @Inject
  public UserController(UserService userService, FormFactory formFactory, utils.Security.Administrator adminAuth, utils.Security.User userAuth) {
    this.userService = userService;
    this.formFactory = formFactory;
    this.adminAuth = adminAuth;
    this.userAuth = userAuth;
  }

  @Transactional
  public Result register() {
    ObjectNode result = Json.newObject();

    String email;
    String password;
    String name;

    // obtenemos datos de la petición post
    DynamicForm requestForm = formFactory.form().bindFromRequest();

    try {
      email = String.valueOf(requestForm.get("email"));
      password = String.valueOf(requestForm.get("password"));
      name = String.valueOf(requestForm.get("name"));
    } catch (Exception ex) {
      result.put("error", "email/password/name null or not string");
      result.put("type", "bad request");
      result.put("message", "Campos incorrectos, contacte con un administrador");
      return badRequest(result);
    }

    // comprobamos si los datos están vacíos y si el email es válido y si ya está registrado
    if (email != null && !email.isEmpty() && password != null && !password.isEmpty() && name != null && !name.isEmpty()) {
      // comprobamos si el email es valido
      Constraints.EmailValidator emailValidator = new Constraints.EmailValidator();
      if (emailValidator.isValid(email)) {
        if (password.length() >= 6) {
          // comprobamos si el email ya está registrado
          if (userService.findByEmail(email) != null) {
            result.put("error", "email registered already");
            result.put("type", "bad request");
            result.put("message", "Este e-mail ya está registrado");
            return badRequest(result);
          }
        } else {
          result.put("error", "password not well generated");
          result.put("type", "bad request");
          result.put("message", "La contraseña no se ha generado bien");
          return badRequest(result);
        }
      } else {
        result.put("error", "email not valid");
        result.put("type", "bad request");
        result.put("message", "Este e-mail no es válido");
        return badRequest(result);
      }

      // intentamos crear usuario
      User user;

      try {
        user = userService.create(email, password, name);
      } catch (Password.CannotPerformOperationException | Password.InvalidHashException ex) {
        Logger.error(ex.getMessage());
        result.put("error", "exception creating hash");
        result.put("type", "internal server error");
        result.put("message", "No hemos podido registrar al usuario. Pruebe de nuevo más tarde");
        return internalServerError(result);
      } catch (javax.persistence.PersistenceException ex) {
        result.put("error", "email registered already");
        result.put("type", "bad request");
        result.put("message", "Este e-mail ya está registrado");
        return badRequest(result);
      }

      // si el usuario devuelto por la persistencia es nulo, ha ido mal
      if (user == null) {
        result.put("error","user null");
        result.put("type", "internal server error");
        result.put("message", "No hemos podido registrar al usuario. Pruebe de nuevo más tarde");
        return internalServerError(result);
      } else {
        // si se ha creado bien
        result.put("ok", "user created");
        result.put("type", "created");
        result.put("message", "Usuario registrado con éxito");
        return created(result);
      }
    } else {
      result.put("error", "empty field or fields");
      result.put("type", "bad request");
      result.put("message", "Ningún campo puede estar vacío");
      return badRequest(result);
    }
  }

  @Transactional(readOnly = true)
  public Result login() {
    ObjectNode result = Json.newObject();
    User user = null;

    String email;
    String password;

    // obtenemos datos de la petición post
    DynamicForm requestForm = formFactory.form().bindFromRequest();

    try {
      email = String.valueOf(requestForm.get("email"));
      password = String.valueOf(requestForm.get("password"));
    } catch (Exception ex) {
      result.put("error", "email/password null or not string");
      result.put("type", "bad request");
      result.put("message", "Campos incorrectos, contacte con un administrador");
      return badRequest(result);
    }

    // comprobamos si los datos están vacíos y si el email es válido y si existe
    if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
      if (password.length() >= 6) {
        // comprobamos email y contraseña
        try {
          user = userService.verifyEmailAndPassword(email, password);

          String token = null;
          // intentamos crear token
          if (user.rol.equals("a")) {
            token = adminAuth.createJWT(user);
          } else if (user.rol.equals("u")) {
            token = userAuth.createJWT(user);
          }

          if (token == null) {
            result.put("error","fail creating JWT");
            result.put("type", "internal server error");
            result.put("message", "No hemos podido identificar al usuario. Pruebe de nuevo más tarde");
            return internalServerError(result);
          } else {
            // si se ha identificado bien
            result.put("ok", "user logged");
            result.put("type", "ok");
            result.put("message", "Usuario identificado con éxito");
            result.put("Authorization", token);
            result.put("userId", user.id);
            result.put("userName", user.name);
            result.put("userRol", user.rol);
            return ok(result);
          }

        } catch (Password.CannotPerformOperationException | Password.InvalidHashException ex) {
          Logger.error(ex.getMessage());
          result.put("error", "exception checking hash");
          result.put("type", "internal server error");
          result.put("message", "No hemos podido identificar al usuario. Pruebe de nuevo más tarde");
        } catch (UserService.UserException e) {
          result.put("error", "incorrect email or password");
          result.put("type", "unauthorized");
          result.put("message", "El correo electrónico o la contraseña no son correctos");
          return unauthorized(result);
        }
      } else {
        result.put("error", "incorrect password");
        result.put("type", "unauthorized");
        result.put("message", "La contraseña no tiene la longitud permitida");
        return unauthorized(result);
      }
    } else {
      result.put("error", "empty field or fields");
      result.put("type", "unauthorized");
      result.put("message", "Ningún campo puede estar vacío");
      return unauthorized(result);
    }

    // comprobar esta salida
    return badRequest("pendiente");
  }

  public Result verifySession() {
    ObjectNode result = Json.newObject();

    if (adminAuth.verifyJWT(Http.Context.current())) {
      // si el token es válido
      result.put("ok", "session active");
      result.put("type", "ok");
      result.put("message", "Sesión activa");
      return ok(result);
    } else {
      result.put("error", "session inactive");
      result.put("type", "unauthorized");
      result.put("message", "Sesión inactiva");
      return unauthorized(result);
    }
  }

}
