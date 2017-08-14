package models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  @Column(nullable = false, unique = true)
  public String email;

  @JsonIgnore
  @Column(nullable = false)
  public String password;

  @Column(length = 20)
  public String name;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @Temporal(TemporalType.DATE)
  public Date registrationDate;

  @Column(length = 1, columnDefinition = "varchar default u")
  public String rol;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  public List<TvShowRequest> requestedTvShows;

  // constructor vacío
  public User() {}

  // constructor por parámetros
  public User(String email, String password, String name) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.rol = "u";
  }

  // constructor copia
  public User(User user) {
    email = user.email;
    password = user.password;
    name = user.name;
    rol = user.rol;
  }

  // toString - solo info importante
  @Override
  public String toString() {
    return "User [id=" + id + ", email=" + email + ", name="
            + name + ", rol=" + rol + ", registrationDate=" + registrationDate + "]";
  }

  // es administrador ?
  public Boolean isAdmin() {
    return (rol.equals("a"));
  }

}
