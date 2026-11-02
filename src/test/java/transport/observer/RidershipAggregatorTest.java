package transport.observer;

import org.junit.jupiter.api.Test;
import transport.TestEvents;
import transport.domain.event.EventType;
import transport.domain.event.HourlyTrafficReportEvent;
import transport.observer.listeners.RidershipAggregator;
import transport.observer.sources.TurnstileSensorSource;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RidershipAggregatorTest {

    private final Clock clock = Clock.fixed(TestEvents.NOW, ZoneOffset.UTC);
    private final TurnstileSensorSource turnstile = new TurnstileSensorSource(clock);
    private final RidershipAggregator aggregator = new RidershipAggregator(clock);
    private final List<HourlyTrafficReportEvent> reports = new ArrayList<>();

    {
        turnstile.subscribe(EventType.PASSENGER_BOARDING, aggregator);
        aggregator.subscribe(EventType.HOURLY_TRAFFIC_REPORT, reports::add);
    }

    @Test
    void aggregatorRepublishesAccumulatedBoardingsAsOneReport() {
        turnstile.registerBoarding("S-1", "R10", 4);
        turnstile.registerBoarding("S-2", "R10", 3);
        turnstile.registerBoarding("S-3", "T3", 3);

        aggregator.publishReport();

        assertEquals(1, reports.size());
        assertEquals(Map.of("R10", 7, "T3", 3), reports.get(0).passengersByRoute());
        assertEquals(10, reports.get(0).totalPassengers());
        assertEquals(TestEvents.NOW, reports.get(0).timestamp());
    }

    @Test
    void reportStartsNewIntervalWithEmptyCounters() {
        turnstile.registerBoarding("S-1", "R10", 4);
        aggregator.publishReport();

        aggregator.publishReport();

        assertEquals(2, reports.size());
        assertEquals(0, reports.get(1).totalPassengers());
    }
}
