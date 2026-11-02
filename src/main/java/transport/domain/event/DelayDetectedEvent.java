package transport.domain.event;

import java.time.Instant;
import java.util.Objects;

public record DelayDetectedEvent(String vehicleId,
                                 String routeId,
                                 DelaySeverity severity,
                                 int delayMinutes,
                                 Instant timestamp) implements TransportEvent {

    public DelayDetectedEvent {
        Objects.requireNonNull(vehicleId);
        Objects.requireNonNull(routeId);
        Objects.requireNonNull(severity);
        Objects.requireNonNull(timestamp);
    }

    @Override
    public EventType type() {
        return EventType.DELAY_DETECTED;
    }
}
