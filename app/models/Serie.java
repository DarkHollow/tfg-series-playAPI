package models;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import play.data.validation.Constraints;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.*;
import json.SerieViews;

@Entity
@Table(name = "serie")
public class Serie {
  public enum Status { Continuing, Ended }

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @JsonView(SerieViews.SearchSerie.class)
  public Integer id;

  @JsonView(SerieViews.InternalFullSerie.class)
  public Integer idTVDB;

  @Column(length = 100)
  @JsonView(SerieViews.SearchSerie.class)
  public String seriesName;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @JsonView(SerieViews.SearchSerie.class)
  public Date firstAired;

  @Column(columnDefinition = "text")
  @JsonView(SerieViews.FullSerie.class)
  public String overview;

  @JsonView(SerieViews.SearchSerie.class)
  public String banner;

  @JsonView(SerieViews.FullSerie.class)
  public String poster;

  @JsonView(SerieViews.FullSerie.class)
  public String fanart;

  @Column(length = 50)
  @JsonView(SerieViews.FullSerie.class)
  public String network;

  @JsonView(SerieViews.FullSerie.class)
  public Integer runtime;

  @JsonView(SerieViews.FullSerie.class)
  @ElementCollection
  public Set<String> genre = new HashSet();

  @JsonView(SerieViews.FullSerie.class)
  public String rating;

  // NOTE: error con H2 en test @Column(columnDefinition = "enum('Continuing', 'Ended')")
  @Enumerated(EnumType.STRING)
  @JsonView(SerieViews.FullSerie.class)
  public Status status;

  @JsonView(SerieViews.FullSerie.class)
  public String writer;

  @JsonView(SerieViews.FullSerie.class)
  public String actors;

  @JsonView(SerieViews.FullSerie.class)
  public Float imdbRating;

  @JsonView(SerieViews.FullSerie.class)
  public String trailer;

  @Transient
  @JsonView(SerieViews.SearchTVDB.class)
  public Boolean local;

  // constructor vacio
  public Serie() {}

  // contructor por campos
  public Serie(Integer idTVDB, String seriesName, Date firstAired,
              String overview, String banner, String poster, String fanart,
              String network, Integer runtime, Set<String> genre,
              String rating, Status status, String writer, String actors,
              Float imdbRating, String trailer) {

    this.idTVDB = idTVDB;
    this.seriesName = seriesName;
    this.firstAired = firstAired;
    this.overview = overview;
    this.banner = banner;
    this.poster = poster;
    this.fanart = fanart;
    this.network = network;
    this.runtime = runtime;
    this.genre = genre;
    this.rating = rating;
    this.status = status;
    this.writer = writer;
    this.actors = actors;
    this.imdbRating = imdbRating;
    this.trailer = trailer;
    local = false;
  }

  // contructor copia
  public Serie(Serie serie) {
    this.idTVDB = serie.idTVDB;
    this.seriesName = serie.seriesName;
    this.firstAired = serie.firstAired;
    this.overview = serie.overview;
    this.banner = serie.banner;
    this.poster = serie.poster;
    this.fanart = serie.fanart;
    this.network = serie.network;
    this.runtime = serie.runtime;
    this.genre = serie.genre;
    this.rating = serie.rating;
    this.status = serie.status;
    this.writer = serie.writer;
    this.actors = serie.actors;
    this.imdbRating = serie.imdbRating;
    this.trailer = serie.trailer;
    this.local = false;
  }

  // solo informacion importante
  @Override
  public String toString() {
    return "Serie [id=" + id + ", idTVDB=" + idTVDB + ", seriesName="
            + seriesName + ", firstAired=" + firstAired + ", overview="
            + overview + ", network=" + network
            + ", status=" + status + "]";
  }
}
