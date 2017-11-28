package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import play.data.validation.Constraints;
import utils.json.JsonViews;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "season")
public class Season {

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  public Integer id;

  @ManyToOne
  @JoinColumn(name = "tvShowId")
  @JsonBackReference
  public TvShow tvShow;

  @Constraints.Required
  public Integer seasonNumber;

  @JsonView(JsonViews.FullAll.class)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  public Date firstAired;

  public String poster;

  @JsonView(JsonViews.FullAll.class)
  public String name;

  @JsonView(JsonViews.FullAll.class)
  @Column(columnDefinition = "text")
  public String overview;

  @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  @JsonView(JsonViews.FullTvShow.class)
  public List<Episode> episodes;

  // constructor vacio
  public Season() {}

  // contructor por campos
  public Season(Integer seasonNumber, Date firstAired, String name, String overview, String poster) {
    this.seasonNumber = seasonNumber;
    this.firstAired = firstAired;
    this.name = name;
    this.overview = overview;
    this.poster = poster;
  }

  // contructor copia
  public Season(Season season) {
    this.seasonNumber = season.seasonNumber;
    this.firstAired = season.firstAired;
    this.name = season.name;
    this.overview = season.overview;
    this.poster = season.poster;
  }

  // poner a null todos las cadenas vacías que no son null
  public void nullify() {
    if (seasonNumber != null) seasonNumber = null;
    if (name != null) name = null;
    if (overview != null && overview.isEmpty()) overview = null;
    if (poster != null && poster.isEmpty()) poster = null;
  }

  // solo informacion importante
  @Override
  public String toString() {
    return "TvShow [id=" + id + ", seasonNumber=" + seasonNumber + ", name=" + name + ", firstAired=" + firstAired
            + ", overview=" + overview +"]";
  }
}
