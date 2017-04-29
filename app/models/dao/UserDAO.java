package models.dao;

import com.google.inject.Inject;
import models.User;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class UserDAO {

  private static String TABLE = User.class.getName();
  private final JPAApi jpa;

  @Inject
  public UserDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public User create(User user) {
    Logger.debug("Persistencia - intentando crear user: " + user.email);
    jpa.em().persist(user);
    jpa.em().flush();
    jpa.em().refresh(user);
    Logger.debug("Persistencia - user añadido: id " + user.id + ", email " + user.email);
    return user;
  }

  // Read - vamos a hacer varios Read

  // Read de busqueda
  // buscar por id
  public User find(Integer id) {
    return jpa.em().find(User.class, id);
  }

  // buscar por email
  public User findByEmail(String email) {
    TypedQuery<User> query = jpa.em().createQuery("SELECT u FROM " + TABLE + " u WHERE u.email = :email", User.class);
    try {
      return query.setParameter("email", email).getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  // buscar por campo exacto
  public List<User> findByExact(String field, String value) {
    TypedQuery<User> query = jpa.em().createQuery("SELECT u FROM " + TABLE + " u WHERE " + field + " = :value", User.class);
    try {
      return query.setParameter("value", value).getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  // buscar por campo LIKE
  public List<User> findByLike(String field, String value) {
    TypedQuery<User> query = jpa.em().createQuery("SELECT s FROM " + TABLE + " s WHERE " + field + " LIKE :value", User.class);
    try {
      return query.setParameter("value", "%" + value + "%").getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  // Read de obtener todas los users
  public List<User> all() {
    return jpa.em().createQuery("SELECT u FROM " + TABLE + " u ORDER BY u.id", User.class).getResultList();
  }

  // Delete
  public void delete(User user) {
    jpa.em().remove(user);
  }
}
