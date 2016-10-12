package models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@Entity
@Table(name = "serie")

public class Serie {
  @Id
  public Integer id;
  public String seriesName;
  public String banner;
  public String poster;
  public List<String> seasonImgs;
  public String seriesId;
  public String status;
  public Date firstAired;
  public String network;
  public Integer runtime;
  public List<String> genre;
  public String overview;
  public Integer lastUpdated;
  public String airsDayOfWeek;
  public String airsTime;
  public String rating;
  public String imdbId;
  public String zap2itId;
  public Double siteRating;
  public Integer siteRatingCount;

  // constructor vacio
  public Serie() {}

  // constructor dado los datos
  public Serie(Integer id, String seriesName, String banner, String poster,
  List<String> seasonImgs, String seriesId, String status, Date firstAired,
  String network, Integer runtime, List<String> genre, String overview,
  Integer lastUpdated, String airsDayOfWeek, String airsTime, String rating,
  String imdbId, String zap2itId, double siteRating, Integer siteRatingCount) {
    this.id = id;
    this.seriesName = seriesName;
    this.banner = banner;
    this.poster = poster;
    this.seasonImgs = seasonImgs;
    this.seriesId = seriesId;
    this.status = status;
    this.firstAired = firstAired;
    this.network = network;
    this.runtime = runtime;
    this.genre = genre;
    this.overview = overview;
    this.lastUpdated = lastUpdated;
    this.airsDayOfWeek = airsDayOfWeek;
    this.airsTime = airsTime;
    this.airsTime = rating;
    this.imdbId = imdbId;
    this.zap2itId = zap2itId;
    this.siteRating = siteRating;
    this.siteRatingCount = siteRatingCount;
  }

  // stringficar
  public String toString() {
    return String.format("Serie id: %s, nombre: %s");
  }

}
