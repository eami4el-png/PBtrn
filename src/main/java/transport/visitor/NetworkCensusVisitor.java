package transport.visitor;

import transport.domain.tree.BusLeaf;
import transport.domain.tree.ChargingHubLeaf;
import transport.domain.tree.NetworkComposite;
import transport.domain.tree.PhysicalStationLeaf;
import transport.domain.tree.RouteComposite;
import transport.domain.tree.TramLeaf;

/**
 * State accumulator: counts vehicles and capacity and averages tram pantograph wear
 * over everything it visits. Stateful, so use one instance per census.
 */
public final class NetworkCensusVisitor implements TransportVisitor<NetworkCensus> {

    private int vehicleCount;
    private int totalCapacity;
    private int tramCount;
    private double pantographWearSum;

    @Override
    public NetworkCensus visit(NetworkComposite network) {
        network.routes().forEach(route -> route.accept(this));
        return snapshot();
    }

    @Override
    public NetworkCensus visit(RouteComposite route) {
        route.elements().forEach(element -> element.accept(this));
        return snapshot();
    }

    @Override
    public NetworkCensus visit(BusLeaf bus) {
        countVehicle(bus.capacity());
        return snapshot();
    }

    @Override
    public NetworkCensus visit(TramLeaf tram) {
        countVehicle(tram.capacity());
        tramCount++;
        pantographWearSum += tram.pantographWearPercent();
        return snapshot();
    }

    @Override
    public NetworkCensus visit(PhysicalStationLeaf station) {
        return snapshot();
    }

    @Override
    public NetworkCensus visit(ChargingHubLeaf chargingHub) {
        return snapshot();
    }

    private void countVehicle(int capacity) {
        vehicleCount++;
        totalCapacity += capacity;
    }

    private NetworkCensus snapshot() {
        double averageWear = tramCount == 0 ? 0.0 : pantographWearSum / tramCount;
        return new NetworkCensus(vehicleCount, totalCapacity, averageWear);
    }
}
