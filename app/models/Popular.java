package models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import utils.json.JsonViews;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "popular")
public class Popular {

  @Id
  @JsonView(JsonViews.SearchTvShow.class)
  public Integer id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  public TvShow tvShow;

  @ElementCollection
  @JsonIgnore
  public List<Integer> requestsCount;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @JsonView(JsonViews.FullTvShow.class)
  public Date updated;

  // constructor vacio
  public Popular() {
    clearRequestsCount();
    updated = new LocalDate().toDate();
  }

  public void updateDays() {
    // comprobación de tamaño
    if (requestsCount.size() != 7) {
      clearRequestsCount();
    }

    LocalDate today = new LocalDate();
    LocalDate lastUpdate = new LocalDate(updated);
    Integer daysBetween = Math.abs(Days.daysBetween(lastUpdate, today).getDays());
    if (!isRequestsCountEmpty()) {
      if (daysBetween >= 7) {
        clearRequestsCount();
      } else {
        for (int i = 0; i < daysBetween; i++) {
          requestsCount.add(0, 0);
          requestsCount.remove(requestsCount.size() - 1);
        }
      }
    }
    // actualizar fecha ultima actualizacion
    if (daysBetween != 0) {
      updated = today.toDate();
    }
  }

  private Boolean isRequestsCountEmpty() {
    return requestsCount.stream().noneMatch(day -> day != 0);
  }

  private void clearRequestsCount() {
    requestsCount = new ArrayList<Integer>() {{
      for (int i = 0; i < 7; i++) {
        add(0);
      }
    }};
  }

  public Integer getPopularity() {
    return requestsCount.stream().reduce(0, (x, y) -> x + y);
  }

}
