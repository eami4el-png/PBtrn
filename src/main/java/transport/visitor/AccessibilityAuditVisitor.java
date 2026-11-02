package transport.visitor;

import transport.domain.tree.BusLeaf;
import transport.domain.tree.ChargingHubLeaf;
import transport.domain.tree.NetworkComposite;
import transport.domain.tree.PhysicalStationLeaf;
import transport.domain.tree.RouteComposite;
import transport.domain.tree.TramLeaf;

import java.util.List;

/** Filter: returns the stations that lack a wheelchair ramp or tactile paving. */
public final class AccessibilityAuditVisitor implements TransportVisitor<List<PhysicalStationLeaf>> {

    @Override
    public List<PhysicalStationLeaf> visit(NetworkComposite network) {
        return network.routes().stream()
                .flatMap(route -> route.accept(this).stream())
                .toList();
    }

    @Override
    public List<PhysicalStationLeaf> visit(RouteComposite route) {
        return route.elements().stream()
                .flatMap(element -> element.accept(this).stream())
                .toList();
    }

    @Override
    public List<PhysicalStationLeaf> visit(BusLeaf bus) {
        return List.of();
    }

    @Override
    public List<PhysicalStationLeaf> visit(TramLeaf tram) {
        return List.of();
    }

    @Override
    public List<PhysicalStationLeaf> visit(PhysicalStationLeaf station) {
        boolean inclusive = station.hasWheelchairRamp() && station.hasTactilePaving();
        return inclusive ? List.of() : List.of(station);
    }

    @Override
    public List<PhysicalStationLeaf> visit(ChargingHubLeaf chargingHub) {
        return List.of();
    }
}
