package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import play.libs.akka.InjectedActorSupport;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class UserManagerActor extends AbstractActor implements InjectedActorSupport {

    private UserActor.Factory childFactory;
    private Config config;

    public static class CreateUser {

        /**
         * Unique id of user to create
         */
        private String id;

        public CreateUser(String id) {
            this.id = id;
        }
    }

    @Inject
    public UserManagerActor(UserActor.Factory childFactory, Config config) {
        this.childFactory = childFactory;
        this.config = config;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateUser.class, createMessage -> {

                    ActorRef userActor =  injectedChild(() -> childFactory.create(createMessage.id),  "user-" + createMessage.id);

                    CompletionStage<Object> future = PatternsCS.ask(userActor, new UserActor.Initialize(), new Timeout(2, TimeUnit.SECONDS));
                    // Send response back to sender once future is done
                    PatternsCS.pipe(future, context().dispatcher()).to(sender());
                })
                .build();
    }
}
