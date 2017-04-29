package models.service;

import com.google.inject.Inject;
import models.User;
import models.dao.UserDAO;

public class UserService {

  private final UserDAO userDAO;

  @Inject
  public UserService(UserDAO userDAO) {
    this.userDAO = userDAO;
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
