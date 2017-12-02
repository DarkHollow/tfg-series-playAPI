package model;

import models.Popular;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class PopularModelTest {

  // testeamos constructor de popular
  @Test
  public void testPopularModelConstructor() {
    Popular popular = new Popular();

    assertEquals(7, popular.requestsCount.size());
    assertEquals(new LocalDate(), new LocalDate(popular.updated));
    for (int i = 0; i < popular.requestsCount.size() - 1; i++) {
      assertEquals((Integer) 0, popular.requestsCount.get(i));
    }
  }

  // testeamos getPopularity de un Popular recien creado
  @Test
  public void testPopularModelConstructorPopularity() {
    Popular popular = new Popular();
    assertEquals((Integer) 0, popular.getPopularity());
  }

  // testeamos getPopularity de un Popular 0 1 2 3 4 5 6 = 21
  @Test
  public void testPopularModelConstructorPopularity21() {
    Popular popular = new Popular();
    assertEquals((Integer) 0, popular.getPopularity());
    for (int i = 0; i < popular.requestsCount.size(); i++) {
      popular.requestsCount.set(i, i);
    }
    assertEquals((Integer) 21, popular.getPopularity());
  }

  @Test
  public void testPopularModelUpdateDays() {
    Popular popular = new Popular();
    // cambiamos la fecha para ver si la actualiza
    popular.updated = new LocalDate().minusDays(4).toDate();
    // ponemos la lista 1 2 3 4 5 6 7
    for (int i = 0; i < popular.requestsCount.size(); i++) {
      popular.requestsCount.set(i, i + 1);
    }

    // actualizamos
    popular.updateDays();

    assertEquals(new LocalDate(), new LocalDate(popular.updated));
    // la lista deberia contener los primeros 4 dias a 0 por no haberse actualizado en 4 dias
    // y tendria que haber movido los 3 restantes
    assertEquals((Integer) 0, popular.requestsCount.get(0));
    assertEquals((Integer) 0, popular.requestsCount.get(1));
    assertEquals((Integer) 0, popular.requestsCount.get(2));
    assertEquals((Integer) 0, popular.requestsCount.get(3));
    assertEquals((Integer) 1, popular.requestsCount.get(4));
    assertEquals((Integer) 2, popular.requestsCount.get(5));
    assertEquals((Integer) 3, popular.requestsCount.get(6));
  }

}
