package transport.observer.sources;

import transport.domain.event.DelayDetectedEvent;
import transport.domain.event.DelaySeverity;
import transport.domain.event.TransportEvent;
import transport.domain.event.VehicleLocationEvent;
import transport.observer.BaseSubject;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class GpsTelemetrySource extends BaseSubject<TransportEvent> {

    private final Clock clock;

    public GpsTelemetrySource(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    public void reportLocation(String vehicleId, String routeId, double latitude, double longitude,
                               double speedKmh, double distanceToNextStopKm) {
        notify(new VehicleLocationEvent(vehicleId, routeId, latitude, longitude,
                speedKmh, distanceToNextStopKm, Instant.now(clock)));
    }

    public void reportDelay(String vehicleId, String routeId, DelaySeverity severity, int delayMinutes) {
        notify(new DelayDetectedEvent(vehicleId, routeId, severity, delayMinutes, Instant.now(clock)));
    }
}
