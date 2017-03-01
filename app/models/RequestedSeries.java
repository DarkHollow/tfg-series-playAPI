package models;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "requestedSeries", uniqueConstraints = @UniqueConstraint(columnNames = {"idTVDB", "usuarioId"}))
public class RequestedSeries {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  public Integer idTVDB;

  // usuario que hace la petición
  @ManyToOne
  @JoinColumn(name = "usuarioId")
  public Usuario usuario;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @Temporal(TemporalType.DATE)
  public Date requestDate;

  // constructor vacío
  public RequestedSeries() {}

  // contructor por parámetros
  public RequestedSeries(Integer idTVDB, Usuario usuario) {
    this.idTVDB = idTVDB;
    this.usuario = usuario;
  }

}
