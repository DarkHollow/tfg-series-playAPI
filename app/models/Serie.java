package models;

import javax.persistence.*;
import java.util.Date;
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
  @Column(length = 50)
  public String network;
  // NOTE: error con H2 en test @Column(columnDefinition = "enum('Continuing', 'Ended')")
  @Enumerated(EnumType.STRING)
  public Status status;

  // constructor vacio
  public Serie() {}

  // contructor por campos
  public Serie(Integer idTVDB, String seriesName, Date firstAired,
              String overview, String banner, String network, Status status) {

    this.idTVDB = idTVDB;
    this.seriesName = seriesName;
    this.firstAired = firstAired;
    this.overview = overview;
    this.banner = banner;
    this.network = network;
    this.status = status;
  }

  // contructor copia
  public Serie(Serie serie) {
    this.idTVDB = serie.idTVDB;
    this.seriesName = serie.seriesName;
    this.firstAired = serie.firstAired;
    this.overview = serie.overview;
    this.banner = serie.banner;
    this.network = serie.network;
    this.status = serie.status;
  }

  @Override
  public String toString() {
    return "Serie [id=" + id + ", idTVDB=" + idTVDB + ", seriesName="
            + seriesName + ", firstAired=" + firstAired + ", overview="
            + overview + ", banner=" + banner + ", network=" + network
            + ", status=" + status + "]";
  }
}
