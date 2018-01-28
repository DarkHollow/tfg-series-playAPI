package models.dao;

import com.google.inject.Inject;
import models.Episode;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.validation.ConstraintViolationException;

public class EpisodeDAO {

  private static String TABLE = Episode.class.getName();
  private final JPAApi jpa;

  @Inject
  public EpisodeDAO(JPAApi jpa) {
    this.jpa = jpa;
  }

  // CRUD

  // Create
  public Episode create(Episode episode) throws ConstraintViolationException {
    jpa.em().persist(episode);
    jpa.em().flush();
    jpa.em().refresh(episode);
    Logger.info("Persistencia - Episodio creado: " + episode.season.tvShow.name + " - Temporada " +
            episode.season.seasonNumber  + " Episodio " + episode.episodeNumber);
    return episode;
  }

  // Read de busqueda
  // buscar por id
  public Episode find(Integer id) {
    return jpa.em().find(Episode.class, id);
  }


  // Delete
  public void delete(Episode episode) {
    jpa.em().remove(episode);
  }
}
