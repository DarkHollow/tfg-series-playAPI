package models.service;

import models.*;
import models.dao.SerieDAO;
import java.util.ArrayList;
import java.util.List;

public class SerieService {

  public static Serie crearSerie(Serie serie) {
    return SerieDAO.create(serie);
  }

  public static List<Serie> todasLasSeries() {
    return SerieDAO.findAllSeries();
  }
}
