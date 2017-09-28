package models;

import javax.persistence.*;

@Entity
public class Evolution {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  public Integer version;

  public String state;

  public Evolution() {}

  public Evolution(Integer version, String state) {
    this.version = version;
    this.state = state;
  }
}
