package transport.domain.event;

import java.time.Instant;
import java.util.Objects;

public record PassengerBoardingEvent(String stationId,
                                     String routeId,
                                     int passengers,
                                     Instant timestamp) implements TransportEvent {

    public PassengerBoardingEvent {
        Objects.requireNonNull(stationId);
        Objects.requireNonNull(routeId);
        Objects.requireNonNull(timestamp);
    }

    @Override
    public EventType type() {
        return EventType.PASSENGER_BOARDING;
    }
}
