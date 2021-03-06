package models;

import com.fasterxml.jackson.annotation.*;
import play.data.validation.Constraints;
import utils.json.JsonViews;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tvShow")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TvShow {
  public enum Status { Continuing, Ended }

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @JsonView(JsonViews.SearchTvShow.class)
  public Integer id;

  @JsonView(JsonViews.InternalFullTvShow.class)
  public String imdbId;

  @Constraints.Required
  @Column(unique = true)
  @JsonView(JsonViews.SearchTvShowTvdbId.class)
  public Integer tvdbId;

  @Column(unique = true)
  @JsonView(JsonViews.SearchTvShowTvdbId.class)
  public Integer tmdbId;

  @Constraints.Required
  @Column(length = 100)
  @JsonView(JsonViews.SearchTvShow.class)
  public String name;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @JsonView(JsonViews.SearchTvShow.class)
  public Date firstAired;

  @Column(columnDefinition = "text")
  @JsonView(JsonViews.FullTvShow.class)
  public String overview;

  @JsonView(JsonViews.SearchTvShow.class)
  public String banner;

  @JsonView(JsonViews.FullTvShow.class)
  public String poster;

  @JsonView(JsonViews.FullTvShow.class)
  public String fanart;

  @Column(length = 50)
  @JsonView(JsonViews.FullTvShow.class)
  public String network;

  @JsonView(JsonViews.FullTvShow.class)
  public String runtime;

  @JsonView(JsonViews.FullTvShow.class)
  @ElementCollection
  public Set<String> genre = new HashSet();

  @JsonView(JsonViews.FullTvShow.class)
  public String rating;

  // NOTE: error con H2 en test @Column(columnDefinition = "enum('Continuing', 'Ended')")
  @Enumerated(EnumType.STRING)
  @JsonView(JsonViews.FullTvShow.class)
  public Status status;

  @JsonView(JsonViews.SearchTvShow.class)
  public Float score;

  @JsonView(JsonViews.SearchTvShow.class)
  public Integer voteCount;

  @Transient
  @JsonView(JsonViews.SearchTVDB.class)
  public Boolean local;

  @Transient
  @JsonView(JsonViews.SearchTVDB.class)
  public String requestStatus;

  @OneToMany(mappedBy = "tvShow", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  @JsonView(JsonViews.FullTvShow.class)
  public List<TvShowVote> tvShowVotes;

  @OneToMany(mappedBy = "tvShow", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  @JsonView(JsonViews.FullTvShow.class)
  public List<Season> seasons;

  @OneToOne(mappedBy = "tvShow", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  public Popular popular;

  @ManyToMany(mappedBy = "followedTvShows")
  @JsonIgnore
  public List<User> followingUsers;

  @JsonIgnore
  public Double twitterRatio;

  // constructor vacio
  public TvShow() {
    twitterRatio = 0D;
  }

  // contructor por campos
  public TvShow(Integer tvdbId, String imdbId, Integer tmdbId, String name, Date firstAired, String overview,
                String banner, String poster, String fanart, String network, String runtime, Set<String> genre,
                String rating, Status status, Float score, Integer voteCount) {

    this.tvdbId = tvdbId;
    this.imdbId = imdbId;
    this.tmdbId = tmdbId;
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
    twitterRatio = 0D;
  }

  // contructor copia
  public TvShow(TvShow tvShow) {
    this.tvdbId = tvShow.tvdbId;
    this.imdbId = tvShow.imdbId;
    this.tmdbId = tvShow.tmdbId;
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
    twitterRatio = 0D;
  }

  // solo informacion importante
  @Override
  public String toString() {
    return "TvShow [id=" + id + ", tvdbId=" + tvdbId + ", tmdbId=" + tmdbId + ", name=" + name +
            ", firstAired=" + firstAired + ", overview=" + overview + ", network=" + network + ", status=" + status +
            ", score=" + score + ", voteCount=" + voteCount +"]";
  }
}
