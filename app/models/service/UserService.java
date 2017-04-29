package models.service;

import com.google.inject.Inject;
import models.User;
import models.dao.UserDAO;
import play.Logger;
import utils.SecurityPassword;

import java.util.Date;

public class UserService {

  private final UserDAO userDAO;
  private final SecurityPassword securityPassword;

  @Inject
  public UserService(UserDAO userDAO, SecurityPassword sr) {
    this.userDAO = userDAO;
    this.securityPassword = sr;
  }

  // registro de usuario nuevo
  public User create(String email, String password, String name)
          throws SecurityPassword.CannotPerformOperationException,
          SecurityPassword.InvalidHashException,
          javax.persistence.PersistenceException {

    // creamos hash
    String hash = securityPassword.createHash(password);

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
