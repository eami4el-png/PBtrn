package transport.domain.tree;

import transport.visitor.TransportVisitor;

import java.util.Objects;

/** fuelPer100Km is liters for DIESEL and kWh for ELECTRIC. */
public record BusLeaf(String id,
                      int capacity,
                      EngineType engineType,
                      double fuelPer100Km,
                      double mileageKm) implements TransportElement {

    public BusLeaf {
        Objects.requireNonNull(id);
        Objects.requireNonNull(engineType);
    }

    @Override
    public <R> R accept(TransportVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
