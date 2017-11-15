package models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import json.TvShowViews;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tvShow")
public class TvShow {
  public enum Status { Continuing, Ended }

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @JsonView(TvShowViews.SearchTvShow.class)
  public Integer id;

  @JsonView(TvShowViews.InternalFullTvShow.class)
  public String imdbId;

  @Constraints.Required
  @Column(unique = true)
  @JsonView(TvShowViews.SearchTvShowTvdbId.class)
  public Integer tvdbId;

  @Constraints.Required
  @Column(length = 100)
  @JsonView(TvShowViews.SearchTvShow.class)
  public String name;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @JsonView(TvShowViews.SearchTvShow.class)
  public Date firstAired;

  @Constraints.Required
  @Column(columnDefinition = "text")
  @JsonView(TvShowViews.FullTvShow.class)
  public String overview;

  @JsonView(TvShowViews.SearchTvShow.class)
  public String banner;

  @JsonView(TvShowViews.FullTvShow.class)
  public String poster;

  @JsonView(TvShowViews.FullTvShow.class)
  public String fanart;

  @Column(length = 50)
  @JsonView(TvShowViews.FullTvShow.class)
  public String network;

  @JsonView(TvShowViews.FullTvShow.class)
  public String runtime;

  @JsonView(TvShowViews.FullTvShow.class)
  @ElementCollection
  public Set<String> genre = new HashSet();

  @JsonView(TvShowViews.FullTvShow.class)
  public String rating;

  // NOTE: error con H2 en test @Column(columnDefinition = "enum('Continuing', 'Ended')")
  @Enumerated(EnumType.STRING)
  @JsonView(TvShowViews.FullTvShow.class)
  public Status status;

  @JsonView(TvShowViews.SearchTvShow.class)
  public Float score;

  @JsonView(TvShowViews.SearchTvShow.class)
  public Integer voteCount;

  @Transient
  @JsonView(TvShowViews.SearchTVDB.class)
  public Boolean local;

  @Transient
  @JsonView(TvShowViews.SearchTVDB.class)
  public String requestStatus;

  @OneToMany(mappedBy = "tvShow", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  @JsonView(TvShowViews.FullTvShow.class)
  public List<TvShowVote> tvShowVotes;

  @OneToMany(mappedBy = "tvShow", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  @JsonView(TvShowViews.FullTvShow.class)
  public List<Season> seasons;

  // constructor vacio
  public TvShow() {}

  // contructor por campos
  public TvShow(Integer tvdbId, String imdbId, String name, Date firstAired, String overview, String banner,
                String poster, String fanart, String network, String runtime, Set<String> genre, String rating,
                Status status, Float score, Integer voteCount) {

    this.tvdbId = tvdbId;
    this.imdbId = imdbId;
    this.name = name;
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
    this.score = score;
    this.voteCount = voteCount;
    local = false;
  }

  // contructor copia
  public TvShow(TvShow tvShow) {
    this.tvdbId = tvShow.tvdbId;
    this.imdbId = tvShow.imdbId;
    this.name = tvShow.name;
    this.firstAired = tvShow.firstAired;
    this.overview = tvShow.overview;
    this.banner = tvShow.banner;
    this.poster = tvShow.poster;
    this.fanart = tvShow.fanart;
    this.network = tvShow.network;
    this.runtime = tvShow.runtime;
    this.genre = tvShow.genre;
    this.rating = tvShow.rating;
    this.status = tvShow.status;
    this.score = tvShow.score;
    this.voteCount = tvShow.voteCount;
    this.local = false;
  }

  // poner a null todos las cadenas vacías que no son null
  public void nullify() {
    if (imdbId != null && imdbId.isEmpty()) imdbId = null;
    if (name != null && name.isEmpty()) name = null;
    if (overview != null && overview.isEmpty()) overview = null;
    if (banner != null && banner.isEmpty()) banner = null;
    if (poster != null && poster.isEmpty()) poster = null;
    if (fanart != null && fanart.isEmpty()) fanart = null;
    if (network != null && network.isEmpty()) network = null;
    if (runtime != null && runtime.isEmpty()) runtime = null;
    if (rating != null && rating.isEmpty()) rating = null;
    if (requestStatus != null && requestStatus.isEmpty()) requestStatus = null;
  }

  // solo informacion importante
  @Override
  public String toString() {
    return "TvShow [id=" + id + ", tvdbId=" + tvdbId + ", name=" + name + ", firstAired=" + firstAired + ", overview="
            + overview + ", network=" + network + ", status=" + status + ", score =" + score
            + ", voteCount=" + voteCount +"]";
  }
}
