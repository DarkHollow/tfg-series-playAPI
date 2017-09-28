package models;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tvShowRequest", uniqueConstraints = @UniqueConstraint(columnNames = {"tvdbId", "userId"}))
public class TvShowRequest {
  public enum Status { Requested, Processing, Persisted, Rejected, Deleted }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  public Integer tvdbId;

  // user que hace la petición
  @ManyToOne
  @JoinColumn(name = "userId")
  public User user;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @Temporal(TemporalType.DATE)
  public Date requestDate;

  @Enumerated(EnumType.STRING)
  public Status status;

  @Column(columnDefinition = "int default 1")
  public Integer requestCount;

  @Enumerated(EnumType.STRING)
  public Status lastStatus;

  // constructor vacío
  public TvShowRequest() {}

  // contructor por parámetros
  public TvShowRequest(Integer tvdbId, User user) {
    this.tvdbId = tvdbId;
    this.user = user;
  }

}
