package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import models.Popular;
import models.TvShow;
import models.service.PopularService;
import models.service.TvShowService;
import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PopularActor extends UntypedActor {

  private JPAApi jpa;
  private TvShowService tvShowService;
  private PopularService popularService;

  @Inject // inyectamos, la doc de playframework esta desactualizada en cuanto a Akka
  public void preStart(final ActorSystem system, @Named("PopularActor") ActorRef popularActor, JPAApi jpa,
                       TvShowService tvShowService, PopularService popularService) {
    this.jpa = jpa;
    this.tvShowService = tvShowService;
    this.popularService = popularService;
    Cancellable cancellable = system.scheduler().schedule(
            Duration.create(0, TimeUnit.MILLISECONDS),
            Duration.create(15, TimeUnit.MINUTES),
            popularActor,"tick", system.dispatcher(),null);
  }

  @Override
  @Transactional
  public void onReceive(Object msg) throws Exception {
    // sea el mensaje que sea, llamar a check

    Logger.info("Actor Popular - actualizando series populares");
    jpa.withTransaction(() -> {

      List<TvShow> tvShows = tvShowService.all();
      tvShows.forEach(tvShow -> {
        if (tvShow.popular != null) {
          tvShow.popular.updateDays();
        } else {
          Logger.info("Serie sin popular, intentando resolverlo...");
          Popular popular = new Popular();
          popular.tvShow = tvShow;
          tvShow.popular = popularService.create(popular);
        }
      });

      }
    );
    Logger.info("Actor Popular - series populares actualizadas");
  }

}
