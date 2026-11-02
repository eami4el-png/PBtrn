package transport.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record HourlyTrafficReportEvent(Map<String, Integer> passengersByRoute,
                                       Instant timestamp) implements TransportEvent {

    public HourlyTrafficReportEvent {
        Objects.requireNonNull(timestamp);
        passengersByRoute = Map.copyOf(passengersByRoute);
    }

    public int totalPassengers() {
        return passengersByRoute.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public EventType type() {
        return EventType.HOURLY_TRAFFIC_REPORT;
    }
}
