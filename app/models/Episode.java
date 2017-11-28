package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import play.data.validation.Constraints;
import utils.json.JsonViews;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "episode")
public class Episode {

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  public Integer id;

  @ManyToOne
  @JoinColumn(name = "seasonId")
  @JsonBackReference
  public Season season;

  @Constraints.Required
  public Integer episodeNumber;

  @JsonView(JsonViews.FullAll.class)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  public Date firstAired;

  public String screenshot;

  @JsonView(JsonViews.FullAll.class)
  public String name;

  @JsonView(JsonViews.FullAll.class)
  @Column(columnDefinition = "text")
  public String overview;

  // constructor vacio
  public Episode() {}

  // contructor por campos
  public Episode(Integer episodeNumber, Date firstAired, String name, String overview, String screenshot) {
    this.episodeNumber = episodeNumber;
    this.firstAired = firstAired;
    this.name = name;
    this.overview = overview;
    this.screenshot = screenshot;
  }

  // contructor copia
  public Episode(Episode season) {
    this.episodeNumber = season.episodeNumber;
    this.firstAired = season.firstAired;
    this.name = season.name;
    this.overview = season.overview;
    this.screenshot = season.screenshot;
  }

  // poner a null todos las cadenas vacías que no son null
  public void nullify() {
    if (episodeNumber != null) episodeNumber = null;
    if (name != null) name = null;
    if (overview != null && overview.isEmpty()) overview = null;
    if (screenshot != null && screenshot.isEmpty()) screenshot = null;
  }

  // solo informacion importante
  @Override
  public String toString() {
    return "Episode [id=" + id + ", episodeNumber=" + episodeNumber + ", name=" + name + ", firstAired=" + firstAired
            + ", overview=" + overview +"]";
  }
}
