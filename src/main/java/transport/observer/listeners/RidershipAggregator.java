package transport.observer.listeners;

import transport.domain.event.HourlyTrafficReportEvent;
import transport.domain.event.PassengerBoardingEvent;
import transport.observer.BaseSubject;
import transport.observer.Observer;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Link of the observer chain: listens to boardings, accumulates passengers per route
 * and, when an interval ends, republishes them as one HourlyTrafficReportEvent.
 */
public final class RidershipAggregator extends BaseSubject<HourlyTrafficReportEvent>
        implements Observer<PassengerBoardingEvent> {

    private final Clock clock;
    private final Map<String, Integer> passengersByRoute = new ConcurrentHashMap<>();

    public RidershipAggregator(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public void onUpdate(PassengerBoardingEvent event) {
        passengersByRoute.merge(event.routeId(), event.passengers(), Integer::sum);
    }

    /** Ends the current interval: drains the counters and notifies subscribers of the report. */
    public void publishReport() {
        Map<String, Integer> drained = new HashMap<>();
        for (String routeId : passengersByRoute.keySet()) {
            Integer passengers = passengersByRoute.remove(routeId);
            if (passengers != null) {
                drained.put(routeId, passengers);
            }
        }
        notify(new HourlyTrafficReportEvent(drained, Instant.now(clock)));
    }
}
