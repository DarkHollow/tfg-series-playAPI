package actors;

import akka.actor.*;
import utils.TVDB;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;
import akka.actor.Props;
import javax.inject.*;

public class TVDBActor extends UntypedActor {

  @Inject // inyectamos, la doc de playframework esta desactualizada en cuanto a Akka
  public void preStart(final ActorSystem system, @Named("TVDBActor") ActorRef tvdbActor) {
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
    TVDB.refreshToken();
  }

}
