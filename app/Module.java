import actors.UserActor;
import actors.UserManagerActor;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

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
        bindActor(UserManagerActor.class, "userManagerActor");
        bindActorFactory(UserActor.class, UserActor.Factory.class);
    }

}
