package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "episodeSeen")
public class EpisodeSeen {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  public Integer id;

  @ManyToOne
  @JoinColumn(name = "userId")
  @JsonBackReference
  public User user;

  @ManyToOne
  @JoinColumn(name = "episodeId")
  @JsonBackReference
  public Episode episode;

  public Date date;

  // constructor vacío
  public EpisodeSeen() {}

  // constructor por parámetros
  public EpisodeSeen(User user, Episode episode, Date date) {
    this.user = user;
    this.episode = episode;
    this.date = date;
  }

}
