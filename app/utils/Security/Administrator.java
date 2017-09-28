package utils.Security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import models.service.UserService;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import views.html.administration.login;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;

public class Administrator extends Roles {
  private final String ROL = "a";

  @Inject
  public Administrator(UserService userService) {
    super(userService);
  }

  @Override
  public String getUsername(Http.Context context) {
    String rawToken = getTokenFromHeader(context);

    if (rawToken != null && rawToken.length() > 6) {
      String token = rawToken.substring(7);

      try {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        DecodedJWT jwt = verifier.verify(token);
        Claim emailClaim = jwt.getClaim("email");
        Claim rolClaim = jwt.getClaim("rol");

        // comprobamos si los claims no son null
        if (!emailClaim.isNull() && !rolClaim.isNull()) {
          // comprobamos si el claim rol es administrador
          if (rolClaim.asString().equals(ROL)) {
            // comprobamos que el usuario exista
            User user = userService.findByEmail(emailClaim.asString());
            if (user != null) {
              // comprobamos que el rol del usuario es de administrador
              if (user.rol.equals(ROL)) {
                return user.email;
              } else {
                Logger.warn("jwt admin - se ha detectado token de usuario que no es administrador");
                return null;
              }
            } else {
              Logger.warn("jwt admin - se ha detectado token de usuario que no existe");
              return null;
            }
          } else {
            Logger.warn("jwt admin - se ha detectado token con claim rol = " + rolClaim.asString());
            return null;
          }
        } else {
          Logger.warn("jwt admin - se ha detectado token sin claim email y/o rol");
          return null;
        }

      } catch (UnsupportedEncodingException ex) {
        Logger.error("jwt admin - verifier.verify(token) ha generado UnsupportedEncodingException");
        return null;
      } catch (JWTVerificationException ex) {
        Logger.debug("jwt admin - verifier.verify(token) ha generado JWTVerificationException");
        return null;
      }
    }

    return null;
  }

  @Override
  public Result onUnauthorized(Http.Context context) {
    if (getTokenFromHeader(context) == null && context.request().path().contains("/admin")) {
      return unauthorized(login.render("Trending Series Administration - Login"));
    }

    ObjectNode result = Json.newObject();
    result.put("error", "Unauthorized");
    result.put("type", "unauthorized");
    result.put("message", "No estás autorizado");
    return unauthorized(result);
  }

}
