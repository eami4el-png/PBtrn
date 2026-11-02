package transport.domain.event;

import java.time.Instant;
import java.util.Objects;

public record VehicleLocationEvent(String vehicleId,
                                   String routeId,
                                   double latitude,
                                   double longitude,
                                   double speedKmh,
                                   double distanceToNextStopKm,
                                   Instant timestamp) implements TransportEvent {

    public VehicleLocationEvent {
        Objects.requireNonNull(vehicleId);
        Objects.requireNonNull(routeId);
        Objects.requireNonNull(timestamp);
    }

    @Override
    public EventType type() {
        return EventType.VEHICLE_LOCATION;
    }
}
