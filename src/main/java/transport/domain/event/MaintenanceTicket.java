package transport.domain.event;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record MaintenanceTicket(String routeId,
                                DelaySeverity cause,
                                List<String> findings,
                                Instant timestamp) implements TransportEvent {

    public MaintenanceTicket {
        Objects.requireNonNull(routeId);
        Objects.requireNonNull(cause);
        Objects.requireNonNull(timestamp);
        findings = List.copyOf(findings);
    }

    @Override
    public EventType type() {
        return EventType.MAINTENANCE_TICKET;
    }
}
