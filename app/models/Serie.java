package models;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import play.data.validation.Constraints;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "serie")
public class Serie {
  public enum Status { Continuing, Ended }

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  public Integer id;
  public Integer idTVDB;
  @Column(length = 100)
  public String seriesName;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  public Date firstAired;
  @Column(columnDefinition = "text")
  public String overview;
  public String banner;
  public String poster;
  public String fanart;
  @Column(length = 50)
  public String network;
  public Integer runtime;
  @ElementCollection
  public Set<String> genre = new HashSet();
  public String rating;
  // NOTE: error con H2 en test @Column(columnDefinition = "enum('Continuing', 'Ended')")
  @Enumerated(EnumType.STRING)
  public Status status;

  // constructor vacio
  public Serie() {}

  // contructor por campos
  public Serie(Integer idTVDB, String seriesName, Date firstAired,
              String overview, String banner, String poster, String fanart,
              String network, Integer runtime, Set<String> genre,
              String rating, Status status) {

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
  }

  @Override
  public String toString() {
    return "Serie [id=" + id + ", idTVDB=" + idTVDB + ", seriesName="
            + seriesName + ", firstAired=" + firstAired + ", overview="
            + overview + ", network=" + network
            + ", status=" + status + "]";
  }
}
