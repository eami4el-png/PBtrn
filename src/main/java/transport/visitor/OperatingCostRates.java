package transport.visitor;

public record OperatingCostRates(double fuelPricePerLiter,
                                 double electricityPricePerKwh,
                                 double tramEnergyKwhPerKm,
                                 double maintenanceCostPerKm) {
}
