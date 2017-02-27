package models.dao;

import com.google.inject.Inject;
import models.Usuario;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class UsuarioDAO {

  private static String TABLE = Usuario.class.getName();
  private final JPAApi jpa;

  @Inject
  public UsuarioDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public Usuario create(Usuario usuario) {
    Logger.debug("Persistencia - intentando crear usuario: " + usuario.email);
    jpa.em().persist(usuario);
    jpa.em().flush();
    jpa.em().refresh(usuario);
    Logger.debug("Persistencia - usuario añadido: id " + usuario.id + ", email " + usuario.email);
    return usuario;
  }

  // Read - vamos a hacer varios Read

  // Read de busqueda
  // buscar por id
  public Usuario find(Integer id) {
    return jpa.em().find(Usuario.class, id);
  }

  // buscar por campo exacto
  public List<Usuario> findByExact(String field, String value) {
    TypedQuery<Usuario> query = jpa.em().createQuery("SELECT u FROM " + TABLE + " u WHERE " + field + " = :value", Usuario.class);
    try {
      return query.setParameter("value", value).getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  // buscar por campo LIKE
  public List<Usuario> findByLike(String field, String value) {
    TypedQuery<Usuario> query = jpa.em().createQuery("SELECT s FROM " + TABLE + " s WHERE " + field + " LIKE :value", Usuario.class);
    try {
      return query.setParameter("value", "%" + value + "%").getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  // Read de obtener todas los usuarios
  public List<Usuario> all() {
    return jpa.em().createQuery("SELECT u FROM " + TABLE + " u ORDER BY u.id", Usuario.class).getResultList();
  }

  // Delete
  public void delete(Usuario usuario) {
    jpa.em().remove(usuario);
  }
}
