package transport.visitor;

import transport.domain.event.DelaySeverity;
import transport.domain.event.MaintenanceTicket;
import transport.domain.tree.BusLeaf;
import transport.domain.tree.ChargingHubLeaf;
import transport.domain.tree.NetworkComposite;
import transport.domain.tree.PhysicalStationLeaf;
import transport.domain.tree.RouteComposite;
import transport.domain.tree.TramLeaf;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Inspects one subtree after an emergency and turns the worn or overloaded elements
 * it finds into a MaintenanceTicket. Stateful, so use one instance per inspection.
 */
public final class RouteEmergencyInspectionVisitor implements TransportVisitor<MaintenanceTicket> {

    static final double BUS_MILEAGE_LIMIT_KM = 300_000;
    static final double PANTOGRAPH_WEAR_LIMIT_PERCENT = 70.0;
    static final double HUB_LOAD_LIMIT_RATIO = 0.9;

    private final String routeId;
    private final DelaySeverity cause;
    private final Instant detectedAt;
    private final List<String> findings = new ArrayList<>();

    public RouteEmergencyInspectionVisitor(String routeId, DelaySeverity cause, Instant detectedAt) {
        this.routeId = routeId;
        this.cause = cause;
        this.detectedAt = detectedAt;
    }

    @Override
    public MaintenanceTicket visit(NetworkComposite network) {
        network.routes().forEach(route -> route.accept(this));
        return ticket();
    }

    @Override
    public MaintenanceTicket visit(RouteComposite route) {
        route.elements().forEach(element -> element.accept(this));
        return ticket();
    }

    @Override
    public MaintenanceTicket visit(BusLeaf bus) {
        if (bus.mileageKm() > BUS_MILEAGE_LIMIT_KM) {
            findings.add(String.format("Bus %s: mileage %.0f km exceeds %.0f km",
                    bus.id(), bus.mileageKm(), BUS_MILEAGE_LIMIT_KM));
        }
        return ticket();
    }

    @Override
    public MaintenanceTicket visit(TramLeaf tram) {
        if (tram.pantographWearPercent() > PANTOGRAPH_WEAR_LIMIT_PERCENT) {
            findings.add(String.format("Tram %s: pantograph wear %.1f%% exceeds %.1f%%",
                    tram.id(), tram.pantographWearPercent(), PANTOGRAPH_WEAR_LIMIT_PERCENT));
        }
        return ticket();
    }

    @Override
    public MaintenanceTicket visit(PhysicalStationLeaf station) {
        return ticket();
    }

    @Override
    public MaintenanceTicket visit(ChargingHubLeaf chargingHub) {
        double loadLimitKw = chargingHub.availablePowerKw() * HUB_LOAD_LIMIT_RATIO;
        if (chargingHub.peakLoadKw() > loadLimitKw) {
            findings.add(String.format("Charging hub %s: peak load %.1f kW exceeds %.1f kW",
                    chargingHub.id(), chargingHub.peakLoadKw(), loadLimitKw));
        }
        return ticket();
    }

    private MaintenanceTicket ticket() {
        return new MaintenanceTicket(routeId, cause, findings, detectedAt);
    }
}
