package models.dao;

import models.*;
import play.db.jpa.*;
import play.Logger;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

public class SerieDAO {

  public static Serie create(Serie serie) {
    JPA.em().persist(serie);
    JPA.em().flush();
    Logger.debug(serie.toString());
    return serie;
  }

  public static List<Serie> findAllSeries() {
    return (List<Serie>) JPA.em().createQuery("SELECT * FROM serie").getResultList();
  }
}
