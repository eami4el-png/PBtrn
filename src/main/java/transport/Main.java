package transport;

import transport.domain.event.EventType;
import transport.domain.event.HourlyTrafficReportEvent;
import transport.domain.event.MaintenanceTicket;
import transport.domain.tree.BusLeaf;
import transport.domain.tree.ChargingHubLeaf;
import transport.domain.tree.EngineType;
import transport.domain.tree.NetworkComposite;
import transport.domain.tree.PhysicalStationLeaf;
import transport.domain.tree.RouteComposite;
import transport.domain.tree.TramLeaf;
import transport.observer.listeners.PassengerFlowAuditor;
import transport.observer.listeners.PassengerMobileAppNotifier;
import transport.observer.listeners.RidershipAggregator;
import transport.observer.listeners.StopDisplayBoard;
import transport.observer.sources.GpsTelemetrySource;
import transport.observer.sources.TurnstileSensorSource;
import transport.service.DelayAlertService;
import transport.service.IntegrationManager;
import transport.visitor.AccessibilityAuditVisitor;
import transport.visitor.FleetOperatingCostVisitor;
import transport.visitor.NetworkCensusVisitor;
import transport.visitor.OperatingCostRates;
import transport.visitor.TransportNetworkJsonExportVisitor;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Composition root: the only place where the object graph is built and wired. */
public final class Main {

    private static final int PRODUCER_THREADS = 3;
    private static final int EVENTS_PER_PRODUCER = 300;
    private static final int CROWDING_THRESHOLD = 200;
    private static final OperatingCostRates COST_RATES = new OperatingCostRates(2.0, 0.5, 4.0, 0.1);

    private Main() {
    }

    public static void main(String[] args) {
        Clock clock = Clock.systemUTC();
        NetworkComposite network = buildNetwork();

        GpsTelemetrySource gps = new GpsTelemetrySource(clock);
        TurnstileSensorSource turnstile = new TurnstileSensorSource(clock);

        StopDisplayBoard displayBoard = new StopDisplayBoard();
        PassengerMobileAppNotifier mobileNotifier = new PassengerMobileAppNotifier();
        DelayAlertService delayAlerts = new DelayAlertService(network);
        PassengerFlowAuditor flowAuditor = new PassengerFlowAuditor(CROWDING_THRESHOLD);
        RidershipAggregator ridership = new RidershipAggregator(clock);

        List<MaintenanceTicket> tickets = new CopyOnWriteArrayList<>();
        List<HourlyTrafficReportEvent> reports = new CopyOnWriteArrayList<>();

        gps.subscribe(EventType.VEHICLE_LOCATION, displayBoard);
        gps.subscribe(EventType.DELAY_DETECTED, mobileNotifier);
        gps.subscribe(EventType.DELAY_DETECTED, delayAlerts);
        turnstile.subscribe(EventType.PASSENGER_BOARDING, flowAuditor);
        turnstile.subscribe(EventType.PASSENGER_BOARDING, ridership);
        ridership.subscribe(EventType.HOURLY_TRAFFIC_REPORT, reports::add);
        delayAlerts.subscribe(EventType.MAINTENANCE_TICKET, tickets::add);

        IntegrationManager manager = new IntegrationManager(PRODUCER_THREADS, gps, turnstile);
        manager.startSimulation(List.of("R10", "T3"), EVENTS_PER_PRODUCER);
        boolean stoppedCleanly = manager.shutdownGracefully();
        ridership.publishReport();

        System.out.println("Producers stopped cleanly: " + stoppedCleanly);
        System.out.println("ETA of V-R10: " + displayBoard.etaMinutes("V-R10") + " min");
        System.out.println("Push notifications sent: " + mobileNotifier.sentNotifications().size());
        System.out.println("Maintenance tickets issued: " + tickets.size());
        System.out.println("Overcrowded stations: " + flowAuditor.overcrowdedStations());
        System.out.println("Traffic report: " + reports.get(0).passengersByRoute()
                + ", total " + reports.get(0).totalPassengers());
        printNetworkAnalysis(network);
    }

    private static void printNetworkAnalysis(NetworkComposite network) {
        System.out.println("Fleet operating cost: " + network.accept(new FleetOperatingCostVisitor(COST_RATES)));
        System.out.println("Network census: " + network.accept(new NetworkCensusVisitor()));
        System.out.println("Stations failing accessibility audit: "
                + network.accept(new AccessibilityAuditVisitor()).stream().map(PhysicalStationLeaf::id).toList());
        System.out.println(network.accept(new TransportNetworkJsonExportVisitor()));
    }

    private static NetworkComposite buildNetwork() {
        RouteComposite busRoute = new RouteComposite("R10", "Bus route 10", List.of(
                new BusLeaf("B-101", 80, EngineType.DIESEL, 30.0, 10_000),
                new BusLeaf("B-102", 100, EngineType.ELECTRIC, 120.0, 20_000),
                new BusLeaf("B-103", 60, EngineType.DIESEL, 25.0, 320_000),
                new PhysicalStationLeaf("S-1", "Central", true, true, true),
                new PhysicalStationLeaf("S-2", "Market", true, false, true)));
        RouteComposite tramRoute = new RouteComposite("T3", "Tram route 3", List.of(
                new TramLeaf("T-301", 150, 600.0, 40.0, 5_000),
                new TramLeaf("T-302", 150, 600.0, 85.0, 7_000),
                new PhysicalStationLeaf("S-3", "Depot", false, false, false),
                new ChargingHubLeaf("H-1", 500.0, 480.0)));
        return new NetworkComposite("NET-1", "City transport network", List.of(busRoute, tramRoute));
    }
}
