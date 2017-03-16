package actors;

import akka.actor.*;
import models.service.tvdb.TvdbConnection;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;

import javax.inject.*;

public class TvdbActor extends UntypedActor {

  private TvdbConnection tvdbConnection;

  @Inject // inyectamos, la doc de playframework esta desactualizada en cuanto a Akka
  public void preStart(final ActorSystem system, @Named("TvdbActor") ActorRef tvdbActor, TvdbConnection tvdbConnection) {
    this.tvdbConnection = tvdbConnection;
    system.scheduler().schedule(
      Duration.create(12, TimeUnit.HOURS),
      Duration.create(12, TimeUnit.HOURS), // frecuencia
      tvdbActor,
      "tick",
      system.dispatcher(),
      null
    );
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    // sea el mensaje que sea, refrescar token

    tvdbConnection.refreshToken();
  }

}
