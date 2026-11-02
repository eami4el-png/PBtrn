package transport.visitor;

import transport.domain.tree.BusLeaf;
import transport.domain.tree.ChargingHubLeaf;
import transport.domain.tree.EngineType;
import transport.domain.tree.NetworkComposite;
import transport.domain.tree.PhysicalStationLeaf;
import transport.domain.tree.RouteComposite;
import transport.domain.tree.TramLeaf;

/** Numeric calculation: carbon footprint in kg CO2 of all vehicles below the visited node. */
public final class EmissionAuditVisitor implements TransportVisitor<Double> {

    private final double kgCo2PerLiterDiesel;
    private final double kgCo2PerKwh;
    private final double tramEnergyKwhPerKm;

    public EmissionAuditVisitor(double kgCo2PerLiterDiesel, double kgCo2PerKwh, double tramEnergyKwhPerKm) {
        this.kgCo2PerLiterDiesel = kgCo2PerLiterDiesel;
        this.kgCo2PerKwh = kgCo2PerKwh;
        this.tramEnergyKwhPerKm = tramEnergyKwhPerKm;
    }

    @Override
    public Double visit(NetworkComposite network) {
        return network.routes().stream().mapToDouble(route -> route.accept(this)).sum();
    }

    @Override
    public Double visit(RouteComposite route) {
        return route.elements().stream().mapToDouble(element -> element.accept(this)).sum();
    }

    @Override
    public Double visit(BusLeaf bus) {
        double energyUnits = bus.mileageKm() / 100.0 * bus.fuelPer100Km();
        double factor = bus.engineType() == EngineType.ELECTRIC ? kgCo2PerKwh : kgCo2PerLiterDiesel;
        return energyUnits * factor;
    }

    @Override
    public Double visit(TramLeaf tram) {
        return tram.mileageKm() * tramEnergyKwhPerKm * kgCo2PerKwh;
    }

    @Override
    public Double visit(PhysicalStationLeaf station) {
        return 0.0;
    }

    @Override
    public Double visit(ChargingHubLeaf chargingHub) {
        return 0.0;
    }
}
