package models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  @Column(nullable = false, unique = true)
  public String email;

  @JsonIgnore
  @Column(nullable = false)
  public String password;

  @JsonIgnore
  @Column(nullable = false)
  public String salt;

  @Column(length = 20)
  public String name;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @Temporal(TemporalType.DATE)
  public Date registrationDate;

  @OneToMany(mappedBy = "usuario")
  public List<RequestedSeries> requestedSeries;

  // constructor vacío
  public Usuario() {}

  // constructor por parámetros
  public Usuario(String email, String password, String salt, String name) {
    this.email = email;
    this.password = password;
    this.salt = salt;
    this.name = name;
  }

  // constructor copia
  public Usuario(Usuario usuario) {
    email = usuario.email;
    password = usuario.password;
    salt = usuario.salt;
    name = usuario.name;
  }

  // toString - solo info importante
  @Override
  public String toString() {
    return "Usuario [id=" + id + ", email=" + email + ", name="
            + name + ", registrationDate=" + registrationDate + "]";
  }

}
