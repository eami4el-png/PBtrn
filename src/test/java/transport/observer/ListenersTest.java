package transport.observer;

import org.junit.jupiter.api.Test;
import transport.TestEvents;
import transport.domain.event.DelaySeverity;
import transport.domain.event.EventType;
import transport.observer.listeners.PassengerFlowAuditor;
import transport.observer.listeners.PassengerMobileAppNotifier;
import transport.observer.listeners.StopDisplayBoard;
import transport.observer.sources.GpsTelemetrySource;
import transport.observer.sources.TurnstileSensorSource;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.OptionalInt;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListenersTest {

    private final Clock clock = Clock.fixed(TestEvents.NOW, ZoneOffset.UTC);
    private final GpsTelemetrySource gps = new GpsTelemetrySource(clock);

    @Test
    void displayBoardRoundsEtaUpToWholeMinutes() {
        StopDisplayBoard board = new StopDisplayBoard();
        gps.subscribe(EventType.VEHICLE_LOCATION, board);

        gps.reportLocation("V-1", "R10", 50.0, 70.0, 40.0, 5.0);

        assertEquals(OptionalInt.of(8), board.etaMinutes("V-1"));
        assertEquals(OptionalInt.empty(), board.etaMinutes("V-2"));
    }

    @Test
    void mobileNotifierSendsTextOnlyForDelays() {
        PassengerMobileAppNotifier notifier = new PassengerMobileAppNotifier();
        gps.subscribe(EventType.DELAY_DETECTED, notifier);

        gps.reportLocation("V-1", "R10", 50.0, 70.0, 40.0, 5.0);
        gps.reportDelay("V-1", "R10", DelaySeverity.CRITICAL_DELAY, 25);

        assertEquals(1, notifier.sentNotifications().size());
        assertTrue(notifier.sentNotifications().get(0).contains("R10"));
        assertTrue(notifier.sentNotifications().get(0).contains("25 min"));
    }

    @Test
    void flowAuditorFlagsStationsReachingThreshold() {
        TurnstileSensorSource turnstile = new TurnstileSensorSource(clock);
        PassengerFlowAuditor auditor = new PassengerFlowAuditor(10);
        turnstile.subscribe(EventType.PASSENGER_BOARDING, auditor);

        turnstile.registerBoarding("S-1", "R10", 6);
        turnstile.registerBoarding("S-1", "R10", 4);
        turnstile.registerBoarding("S-2", "R10", 9);

        assertEquals(10, auditor.passengersAt("S-1"));
        assertEquals(Set.of("S-1"), auditor.overcrowdedStations());
    }
}
