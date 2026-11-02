package transport.visitor;

import transport.domain.tree.BusLeaf;
import transport.domain.tree.ChargingHubLeaf;
import transport.domain.tree.NetworkComposite;
import transport.domain.tree.PhysicalStationLeaf;
import transport.domain.tree.RouteComposite;
import transport.domain.tree.TramLeaf;

/**
 * One overloaded visit(...) per element type. Overload resolution happens at
 * compile time on the static type, which is why elements need accept(): it is
 * the second dispatch that lets the runtime type of the element pick the overload.
 */
public interface TransportVisitor<R> {

    R visit(NetworkComposite network);

    R visit(RouteComposite route);

    R visit(BusLeaf bus);

    R visit(TramLeaf tram);

    R visit(PhysicalStationLeaf station);

    R visit(ChargingHubLeaf chargingHub);
}
