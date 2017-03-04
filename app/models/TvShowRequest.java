package models;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tvShowRequest", uniqueConstraints = @UniqueConstraint(columnNames = {"tvdbId", "usuarioId"}))
public class TvShowRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  public Integer tvdbId;

  // usuario que hace la petición
  @ManyToOne
  @JoinColumn(name = "usuarioId")
  public Usuario usuario;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @Temporal(TemporalType.DATE)
  public Date requestDate;

  // constructor vacío
  public TvShowRequest() {}

  // contructor por parámetros
  public TvShowRequest(Integer tvdbId, Usuario usuario) {
    this.tvdbId = tvdbId;
    this.usuario = usuario;
  }

}
