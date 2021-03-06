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

  // Get top popular by size
  private List<Popular> getPopularSize(Integer size) {
    List<Popular> populars = popularDAO.all().stream().sorted(Comparator.comparing(Popular::getPopularity).reversed()).collect(Collectors.toList());
    populars.removeIf(popular -> popular.getPopularity() == 0);
    if (populars.isEmpty()) {
      return populars;
    } else if (populars.size() < size) {
      return populars.subList(0, populars.size());
    } else {
      return populars.subList(0, size);
    }
  }

  // Get top 1-10 popular
  public List<Popular> getPopular(Integer size) {
    return getPopularSize(size);
  }

  // Get top 30 popular for twitter
  public List<Popular> getTwitterPopular() {
    return getPopularSize(30).stream().sorted(Comparator.comparing((Popular popular) -> popular.tvShow.twitterRatio)
    .thenComparing(Popular::getPopularity).reversed()).collect(Collectors.toList());
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
