package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import models.Popular;
import models.service.PopularService;
import models.service.external.TwitterService;
import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TwitterActor extends UntypedActor {

  private JPAApi jpa;
  private TwitterService twitterService;
  private PopularService popularService;

  @Inject // inyectamos, la doc de playframework esta desactualizada en cuanto a Akka
  public void preStart(final ActorSystem system, @Named("TwitterActor") ActorRef twitterActor, JPAApi jpa,
                       TwitterService twitterService, PopularService popularService) {
    this.jpa = jpa;
    this.twitterService = twitterService;
    this.popularService = popularService;
    Cancellable cancellable = system.scheduler().schedule(
            Duration.create(0, TimeUnit.MILLISECONDS),
            Duration.create(16, TimeUnit.MINUTES),
            twitterActor,"tick", system.dispatcher(),null);
  }

  @Override
  @Transactional
  public void onReceive(Object msg) throws Exception {
    // sea el mensaje que sea, llamar a check

    Logger.info("Actor TwitterService - getting ratios");
    jpa.withTransaction(() -> {
      List<Popular> populars = popularService.getTwitterPopular();
      populars.forEach(popular -> {
        popular.tvShow.twitterRatio = twitterService.getRatio(popular.tvShow.name.replaceAll("\\s", ""));
      });
    });
    Logger.info("Actor TwitterService - getting ratios terminado");
  }

}
