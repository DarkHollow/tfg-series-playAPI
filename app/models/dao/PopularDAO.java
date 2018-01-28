package models.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import models.Popular;
import play.Logger;
import play.db.jpa.JPAApi;

import java.util.List;

public class PopularDAO {

  private static String TABLE = Popular.class.getName();
  private final JPAApi jpa;

  @Inject
  public PopularDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public Popular create(Popular popular) {
    jpa.em().persist(popular);
    jpa.em().flush();
    jpa.em().refresh(popular);
    return popular;
  }

  // find by id
  public Popular find(Integer id) {
    return jpa.em().find(Popular.class, id);
  }

  // get all
  public List<Popular> all() {
    return jpa.em().createQuery("SELECT t FROM " + TABLE + " t", Popular.class).getResultList();
  }

  public void delete(Popular popular) {
    jpa.em().remove(popular);
  }

  public Popular growPopularity(Integer id) {
    Popular popular = find(id);
    try {
      if (!jpa.em().getTransaction().isActive()) {
        jpa.em().getTransaction().begin();
        popular.updateDays();
        popular.requestsCount.set(0, popular.requestsCount.get(0) + 1);
        popular.requestsCount = Lists.reverse(popular.requestsCount);
        jpa.em().getTransaction().commit();
        jpa.em().refresh(popular);
        return popular;
      }
    } catch (Exception ex) {
      Logger.error("Error growPopularity");
      jpa.em().getTransaction().rollback();
    }
    return popular;
  }

}
