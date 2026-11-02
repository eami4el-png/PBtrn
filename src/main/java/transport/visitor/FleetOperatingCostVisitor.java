package transport.visitor;

import transport.domain.tree.BusLeaf;
import transport.domain.tree.ChargingHubLeaf;
import transport.domain.tree.EngineType;
import transport.domain.tree.NetworkComposite;
import transport.domain.tree.PhysicalStationLeaf;
import transport.domain.tree.RouteComposite;
import transport.domain.tree.TramLeaf;

import java.util.Objects;

/** Numeric calculation: total energy plus maintenance cost of all vehicles below the visited node. */
public final class FleetOperatingCostVisitor implements TransportVisitor<Double> {

    private final OperatingCostRates rates;

    public FleetOperatingCostVisitor(OperatingCostRates rates) {
        this.rates = Objects.requireNonNull(rates);
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
        double unitPrice = bus.engineType() == EngineType.ELECTRIC
                ? rates.electricityPricePerKwh()
                : rates.fuelPricePerLiter();
        return energyUnits * unitPrice + maintenanceCost(bus.mileageKm());
    }

    @Override
    public Double visit(TramLeaf tram) {
        double energyCost = tram.mileageKm() * rates.tramEnergyKwhPerKm() * rates.electricityPricePerKwh();
        return energyCost + maintenanceCost(tram.mileageKm());
    }

    @Override
    public Double visit(PhysicalStationLeaf station) {
        return 0.0;
    }

    @Override
    public Double visit(ChargingHubLeaf chargingHub) {
        return 0.0;
    }

    private double maintenanceCost(double mileageKm) {
        return mileageKm * rates.maintenanceCostPerKm();
    }
}
