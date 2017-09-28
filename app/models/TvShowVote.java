package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "tvShowVote")
public class TvShowVote {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  public Integer id;

  @ManyToOne
  @JoinColumn(name = "userId")
  @JsonBackReference
  public User user;

  @ManyToOne
  @JoinColumn(name = "tvShowId")
  @JsonBackReference
  public TvShow tvShow;

  public Float score;

  // constructor vacío
  public TvShowVote() {}

  // constructor por parámetros
  public TvShowVote(User user, TvShow tvShow, Float score) {
    this.user = user;
    this.tvShow = tvShow;
    this.score = score;
  }

  // constructor copia
  public TvShowVote(TvShowVote tvShowVote) {
    user = tvShowVote.user;
    tvShow = tvShowVote.tvShow;
    score = tvShowVote.score;
  }

  // toString - solo info importante
  @Override
  public String toString() {
    return "TvShowVote [id=" + id + ", user=" + user.email + ", tvShow="
            + tvShow.id + " - " + tvShow.name + ", score=" + score + "]";
  }

}
