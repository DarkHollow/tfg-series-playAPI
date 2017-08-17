package models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import json.TvShowViews;

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

  @Column(unique = true)
  @JsonView(TvShowViews.SearchTvShowTvdbId.class)
  public Integer tvdbId;

  @Column(length = 100)
  @JsonView(TvShowViews.SearchTvShow.class)
  public String name;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @JsonView(TvShowViews.SearchTvShow.class)
  public Date firstAired;

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

  @OneToMany(mappedBy = "tvShow", cascade = CascadeType.ALL)
  @JsonManagedReference
  @JsonIgnore
  public List<TvShowVote> tvShowVotes;

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

  // solo informacion importante
  @Override
  public String toString() {
    return "TvShow [id=" + id + ", tvdbId=" + tvdbId + ", name=" + name + ", firstAired=" + firstAired + ", overview="
            + overview + ", network=" + network + ", status=" + status + ", score =" + score
            + ", voteCount=" + voteCount +"]";
  }
}
