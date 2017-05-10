package models.service;

import com.google.inject.Inject;
import models.User;
import models.dao.UserDAO;
import play.Logger;
import utils.Security.Password;

import java.util.Date;

public class UserService {

  private final UserDAO userDAO;
  private final Password sp;

  static public class UserException extends Exception {
    UserException(String message) {
      super(message);
    }
  }

  @Inject
  public UserService(UserDAO userDAO, Password sp) {
    this.userDAO = userDAO;
    this.sp = sp;
  }

  // registro de usuario nuevo
  public User create(String email, String password, String name)
          throws Password.CannotPerformOperationException,
          Password.InvalidHashException,
          javax.persistence.PersistenceException {

    // creamos hash
    String hash = sp.createHash(password);

    // creamos usuario
    User user = new User();
    user.email = email;
    user.password = hash;
    user.name = name;
    user.registrationDate = new Date();
    user = userDAO.create(user);

    if (user == null) {
      Logger.debug("Error creando usuario");
    }

    return user;
  }

  // Read de busqueda
  // buscar por id
  public User find(Integer id) {
    return userDAO.find(id);
  }

  // buscar por email
  public User findByEmail(String email) {
    return userDAO.findByEmail(email);
  }

}
