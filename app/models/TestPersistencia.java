package models;

import javax.persistence.*;
import play.data.validation.Constraints;

@Entity
public class TestPersistencia {
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  public Integer id;
  @Constraints.Required
  public String value;

  // constructor vacio
  public TestPersistencia() {}
}
