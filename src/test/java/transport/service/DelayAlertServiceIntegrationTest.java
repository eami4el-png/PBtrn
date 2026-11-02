package transport.service;

import org.junit.jupiter.api.Test;
import transport.SampleNetwork;
import transport.TestEvents;
import transport.domain.event.DelaySeverity;
import transport.domain.event.EventType;
import transport.domain.event.MaintenanceTicket;
import transport.observer.sources.GpsTelemetrySource;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DelayAlertServiceIntegrationTest {

    private final GpsTelemetrySource gps = new GpsTelemetrySource(Clock.fixed(TestEvents.NOW, ZoneOffset.UTC));
    private final List<MaintenanceTicket> tickets = new ArrayList<>();

    {
        DelayAlertService alertService = new DelayAlertService(SampleNetwork.network());
        gps.subscribe(EventType.DELAY_DETECTED, alertService);
        alertService.subscribe(EventType.MAINTENANCE_TICKET, tickets::add);
    }

    @Test
    void criticalDelayInspectsOnlyTheAffectedRouteSubtree() {
        gps.reportDelay("V-T3", "T3", DelaySeverity.CRITICAL_DELAY, 25);

        assertEquals(1, tickets.size());
        MaintenanceTicket ticket = tickets.get(0);
        assertEquals("T3", ticket.routeId());
        assertEquals(DelaySeverity.CRITICAL_DELAY, ticket.cause());
        assertEquals(TestEvents.NOW, ticket.timestamp());
        assertEquals(List.of(
                "Tram T-302: pantograph wear 85.0% exceeds 70.0%",
                "Charging hub H-1: peak load 480.0 kW exceeds 450.0 kW"), ticket.findings());
    }

    @Test
    void minorDelaysAndUnknownRoutesIssueNoTicketWhileAccidentDoes() {
        gps.reportDelay("V-R10", "R10", DelaySeverity.MINOR_DELAY, 5);
        gps.reportDelay("V-R99", "R99", DelaySeverity.ACCIDENT, 40);

        assertEquals(0, tickets.size());

        gps.reportDelay("V-R10", "R10", DelaySeverity.ACCIDENT, 40);

        assertEquals(1, tickets.size());
        assertEquals(DelaySeverity.ACCIDENT, tickets.get(0).cause());
        assertEquals(1, tickets.get(0).findings().size());
        assertTrue(tickets.get(0).findings().get(0).contains("B-103"));
    }
}
