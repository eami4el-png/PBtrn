package transport;

import transport.domain.event.DelayDetectedEvent;
import transport.domain.event.DelaySeverity;
import transport.domain.event.VehicleLocationEvent;

import java.time.Instant;

public final class TestEvents {

    public static final Instant NOW = Instant.parse("2026-01-01T08:00:00Z");

    private TestEvents() {
    }

    public static VehicleLocationEvent location(String vehicleId) {
        return new VehicleLocationEvent(vehicleId, "R10", 50.0, 70.0, 30.0, 6.0, NOW);
    }

    public static DelayDetectedEvent delay(String vehicleId, DelaySeverity severity) {
        return new DelayDetectedEvent(vehicleId, "R10", severity, 15, NOW);
    }
}
