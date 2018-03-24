package controllers;

import actors.UserManagerActor;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.pattern.PatternsCS;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import org.webjars.play.WebJarsUtil;
import play.libs.F;
import play.mvc.*;

import views.html.*;

import javax.inject.Named;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private ActorRef userManagerActor;
    private WebJarsUtil webJarsUtil;

    @Inject
    public HomeController(@Named("userManagerActor") ActorRef userManagerActor, WebJarsUtil webJarsUtil) {
        this.userManagerActor = userManagerActor;
        this.webJarsUtil = webJarsUtil;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(index.render(request(), webJarsUtil));
    }

    public WebSocket ws(){

//        return WebSocket.Text.accept(request -> {
//
//            // In sink = client to us
//            Sink<String, ?> in = Sink.foreach(System.out::println);
//
//            // Out source = us to client
//            Source<String, ?> out = Source.single("Hello!").concat(Source.maybe());
//            Flow<String, String, ?> websocketFlow = Flow.fromSinkAndSource(in, out);
//            return websocketFlow;
//        });

        // 1) Says that websocket accepts text communication
        // 2) Websocket is modeled as a flow
//        return WebSocket.Text.acceptOrResult(request -> {
//
//            // In sink = client to us
//            Sink<String, ?> in = Sink.foreach(System.out::println);
//
//            // Out source = us to client
//            Source<String, ?> out = Source.single("Hello!").concat(Source.maybe());
//            Flow<String, String, ?> websocketFlow = Flow.fromSinkAndSource(in, out);
//
//            return CompletableFuture.completedFuture(F.Either.Right(websocketFlow));
//        });

        return WebSocket.Json.acceptOrResult(request -> {

            String userId = Long.toString(request().asScala().id());

            return PatternsCS.ask(userManagerActor, new UserManagerActor.CreateUser(userId), new Timeout(2, TimeUnit.SECONDS))
                    .thenApply(o -> F.Either.Right((Flow<JsonNode, JsonNode, NotUsed>)o));


        });

    }

}
