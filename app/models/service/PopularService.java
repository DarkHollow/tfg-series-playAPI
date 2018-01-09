package models.service;

import com.google.inject.Inject;
import models.Popular;
import models.TvShow;
import models.dao.PopularDAO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PopularService {

  private final PopularDAO popularDAO;

  @Inject
  public PopularService(PopularDAO popularDAO) {
    this.popularDAO = popularDAO;
  }

  // CRUD

  // Create
  public Popular create(Popular popular) {
    return popularDAO.create(popular);
  }

  // Get top 10 popular
  public List<Popular> getTop10() {
    List<Popular> populars = popularDAO.all().stream().sorted(Comparator.comparing(Popular::getPopularity).reversed()).collect(Collectors.toList());
    if (populars.isEmpty()) {
      return null;
    } else if (populars.size() < 10) {
      return populars.subList(0, populars.size());
    } else {
      return populars.subList(0, 10);
    }
  }

  // Delete por id
  public Boolean delete(Integer id) {
    Popular popular = popularDAO.find(id);

    if (popular != null && popular.tvShow != null) {
      // me elimino de mis padres
      popular.tvShow.popular = null;

      // elimino mi serie asignada
      popular.tvShow = null;

      // finalmente, me elimino yo
      popularDAO.delete(popular);
      return true;
    } else {
      return false;
    }
  }

  public Integer growPopularity(TvShow tvShow) {
    tvShow.popular = popularDAO.growPopularity(tvShow.id);
    return tvShow.popular.getPopularity();
  }

}
