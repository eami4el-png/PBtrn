package transport.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import transport.TestEvents;
import transport.domain.event.DelaySeverity;
import transport.domain.event.EventType;
import transport.domain.event.TransportEvent;
import transport.observer.Observer;
import transport.observer.sources.GpsTelemetrySource;
import transport.observer.sources.TurnstileSensorSource;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(60)
class ConcurrencyStressTest {

    private static final int PRODUCERS = 4;
    private static final int EVENTS_PER_PRODUCER = 1000;

    private final Clock clock = Clock.fixed(TestEvents.NOW, ZoneOffset.UTC);
    private final GpsTelemetrySource gps = new GpsTelemetrySource(clock);
    private final TurnstileSensorSource turnstile = new TurnstileSensorSource(clock);

    @Test
    void fourProducersThousandEventsEachAreAllDelivered() {
        AtomicInteger delivered = new AtomicInteger();
        gps.subscribe(EventType.DELAY_DETECTED, event -> delivered.incrementAndGet());
        IntegrationManager manager = new IntegrationManager(PRODUCERS, gps, turnstile);

        for (int producer = 0; producer < PRODUCERS; producer++) {
            manager.submit(() -> publishDelays(EVENTS_PER_PRODUCER));
        }

        assertTrue(manager.shutdownGracefully());
        assertEquals(PRODUCERS * EVENTS_PER_PRODUCER, delivered.get());
    }

    @Test
    void subscribingAndUnsubscribingWhilePublishingLosesNoEvents() {
        AtomicInteger delivered = new AtomicInteger();
        gps.subscribe(EventType.DELAY_DETECTED, event -> delivered.incrementAndGet());
        IntegrationManager manager = new IntegrationManager(PRODUCERS + 1, gps, turnstile);

        for (int producer = 0; producer < PRODUCERS; producer++) {
            manager.submit(() -> publishDelays(EVENTS_PER_PRODUCER));
        }
        manager.submit(this::toggleTransientObserver);

        assertTrue(manager.shutdownGracefully());
        assertEquals(PRODUCERS * EVENTS_PER_PRODUCER, delivered.get());
    }

    @Test
    void gracefulShutdownFinishesAllQueuedProducersAndRejectsNewOnes() {
        AtomicInteger locations = new AtomicInteger();
        AtomicInteger delays = new AtomicInteger();
        AtomicInteger boardings = new AtomicInteger();
        gps.subscribe(EventType.VEHICLE_LOCATION, event -> locations.incrementAndGet());
        gps.subscribe(EventType.DELAY_DETECTED, event -> delays.incrementAndGet());
        turnstile.subscribe(EventType.PASSENGER_BOARDING, event -> boardings.incrementAndGet());
        IntegrationManager manager = new IntegrationManager(1, gps, turnstile);

        manager.startSimulation(List.of("R10", "T3"), EVENTS_PER_PRODUCER);

        assertTrue(manager.shutdownGracefully());
        assertEquals(EVENTS_PER_PRODUCER, locations.get());
        assertEquals(EVENTS_PER_PRODUCER, delays.get());
        assertEquals(EVENTS_PER_PRODUCER, boardings.get());
        assertThrows(RejectedExecutionException.class, () -> manager.submit(() -> { }));
    }

    private void publishDelays(int count) {
        for (int i = 0; i < count; i++) {
            gps.reportDelay("V-" + i, "R10", DelaySeverity.MINOR_DELAY, 5);
        }
    }

    private void toggleTransientObserver() {
        Observer<TransportEvent> transientObserver = event -> { };
        for (int i = 0; i < EVENTS_PER_PRODUCER; i++) {
            gps.subscribe(EventType.DELAY_DETECTED, transientObserver);
            gps.unsubscribe(EventType.DELAY_DETECTED, transientObserver);
        }
    }
}
