package actors;

import akka.Done;
import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dtos.DataPayload;
import play.libs.Json;
import scala.concurrent.duration.FiniteDuration;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserActor extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private int dataNumber = 0;

    private final Sink<JsonNode, NotUsed> hubSink;
    private final Flow<JsonNode, JsonNode, NotUsed> websocketFlow;
    private String id;
    private Materializer mat;

    @Inject
    public UserActor(@Assisted String id, Materializer mat) {
        this.id = id;
        this.mat = mat;

        Pair<Sink<JsonNode, NotUsed>, Source<JsonNode, NotUsed>> sinkSourcePair =
                MergeHub.of(JsonNode.class, 16)
                        .toMat(BroadcastHub.of(JsonNode.class, 256), Keep.both())
                        .run(mat);

        this.hubSink = sinkSourcePair.first();

        // us to clients (from broadcastHub)
        Source<JsonNode, NotUsed> hubSource = sinkSourcePair.second();

        // clients to us
        Sink<JsonNode, CompletionStage<Done>> jsonSink = Sink.foreach(this::onClientRequset);

        // Shutdown logic
        this.websocketFlow = Flow.fromSinkAndSourceCoupled(jsonSink, hubSource).watchTermination(this::onSocketClosed);
    }

    private NotUsed onSocketClosed(NotUsed n, CompletionStage<Done> stage) {
        stage.thenAccept(f -> context().stop(self()));
        return NotUsed.getInstance();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Initialize.class, watchData -> {
            logger.info("Setup data watch");
//            Source.single(Json.toJson("Hello!")).to(this.hubSink).run(mat);
//            Source.single(Json.toJson("Hello!")).to(this.hubSink).run(mat);
//            Source.single(Json.toJson("Hello!")).to(this.hubSink).run(mat);

            sender().tell(websocketFlow, self());
        }).build();
    }

    private void onClientRequset(JsonNode input) {
        System.out.println("Got new client request");
        System.out.println(input.toString());

        addNewDataSource();
    }

    private void addNewDataSource(){

        int setNumber = ++dataNumber;

        Random random = new Random();

        Source.tick(FiniteDuration.Zero(), FiniteDuration.apply(1, TimeUnit.SECONDS), "tick")
                .mapAsync(1, obj -> CompletableFuture.supplyAsync(() -> IntStream.rangeClosed(1,10)
                        .map(num -> random.nextInt(100)+1).boxed().collect(Collectors.toList())))
                .map(items -> Json.toJson(new DataPayload(setNumber, items)))
                .to(this.hubSink).run(mat);
    }

    public interface Factory {
        Actor create(String id);
    }

    public static class Initialize {
        public Initialize() {
        }
    }

}
