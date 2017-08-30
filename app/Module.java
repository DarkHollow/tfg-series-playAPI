import actors.TvdbActor;
import com.google.inject.AbstractModule;
import models.service.external.TvdbConnection;
import play.libs.akka.AkkaGuiceSupport;

import java.time.Clock;

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
public class Module extends AbstractModule implements AkkaGuiceSupport {

    @Override
    public void configure() {
        // Use the system clock as the default implementation of Clock
        bind(Clock.class).toInstance(Clock.systemDefaultZone());
        // Inicializar la clase TvdbConnection para hacer login y mas
        bind(TvdbConnection.class).asEagerSingleton();
        // bindeamos el actor de TvdbConnection
        bindActor(TvdbActor.class, "TvdbActor");
    }

}
