package transport.service;

import transport.domain.event.DelaySeverity;
import transport.observer.sources.GpsTelemetrySource;
import transport.observer.sources.TurnstileSensorSource;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

/** Runs the producer threads that feed the sources and shuts them down gracefully. */
public final class IntegrationManager {

    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;
    private static final int STATION_COUNT = 3;
    private static final double BASE_LATITUDE = 50.0;
    private static final double BASE_LONGITUDE = 70.0;
    private static final double COORDINATE_STEP = 0.0001;
    private static final int MIN_SPEED_KMH = 20;
    private static final int SPEED_RANGE_KMH = 30;
    private static final int MAX_DISTANCE_KM = 5;
    private static final int MAX_BOARDING_GROUP = 5;
    private static final int BASE_DELAY_MINUTES = 5;
    private static final int DELAY_RANGE_MINUTES = 20;

    private final ExecutorService executor;
    private final GpsTelemetrySource gps;
    private final TurnstileSensorSource turnstile;

    public IntegrationManager(int producerThreads, GpsTelemetrySource gps, TurnstileSensorSource turnstile) {
        this.executor = Executors.newFixedThreadPool(producerThreads);
        this.gps = Objects.requireNonNull(gps);
        this.turnstile = Objects.requireNonNull(turnstile);
    }

    public void submit(Runnable producer) {
        executor.execute(producer);
    }

    /** Starts three independent producers: telemetry, ticket validation and schedule failures. */
    public void startSimulation(List<String> routeIds, int eventsPerProducer) {
        submit(() -> repeat(eventsPerProducer, i -> {
            String routeId = routeAt(routeIds, i);
            gps.reportLocation("V-" + routeId, routeId,
                    BASE_LATITUDE + i * COORDINATE_STEP, BASE_LONGITUDE + i * COORDINATE_STEP,
                    MIN_SPEED_KMH + i % SPEED_RANGE_KMH, 1 + i % MAX_DISTANCE_KM);
        }));
        submit(() -> repeat(eventsPerProducer, i ->
                turnstile.registerBoarding("S-" + (1 + i % STATION_COUNT), routeAt(routeIds, i),
                        1 + i % MAX_BOARDING_GROUP)));
        submit(() -> repeat(eventsPerProducer, i -> {
            String routeId = routeAt(routeIds, i);
            DelaySeverity severity = DelaySeverity.values()[i % DelaySeverity.values().length];
            gps.reportDelay("V-" + routeId, routeId, severity, BASE_DELAY_MINUTES + i % DELAY_RANGE_MINUTES);
        }));
    }

    /** Lets queued producers finish; interrupts them only if they exceed the timeout. */
    public boolean shutdownGracefully() {
        executor.shutdown();
        try {
            if (executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                return true;
            }
            executor.shutdownNow();
            return executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static String routeAt(List<String> routeIds, int index) {
        return routeIds.get(index % routeIds.size());
    }

    private static void repeat(int times, IntConsumer action) {
        for (int i = 0; i < times; i++) {
            action.accept(i);
        }
    }
}
