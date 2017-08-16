package models;

import javax.persistence.*;

@Entity
@Table(name = "tvShowVote")
public class TvShowVote {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  @ManyToOne
  @JoinColumn(name = "userId")
  public User user;

  @ManyToOne
  @JoinColumn(name = "tvShowId")
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
