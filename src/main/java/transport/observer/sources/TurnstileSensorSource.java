package transport.observer.sources;

import transport.domain.event.PassengerBoardingEvent;
import transport.observer.BaseSubject;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class TurnstileSensorSource extends BaseSubject<PassengerBoardingEvent> {

    private final Clock clock;

    public TurnstileSensorSource(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    public void registerBoarding(String stationId, String routeId, int passengers) {
        notify(new PassengerBoardingEvent(stationId, routeId, passengers, Instant.now(clock)));
    }
}
