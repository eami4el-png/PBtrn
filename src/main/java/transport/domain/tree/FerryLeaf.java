package transport.domain.tree;

import transport.visitor.TransportVisitor;

import java.util.Objects;

/** River tram: diesel-powered, fuelPer100Km is liters. */
public record FerryLeaf(String id,
                        int capacity,
                        double fuelPer100Km,
                        double mileageKm) implements TransportElement {

    public FerryLeaf {
        Objects.requireNonNull(id);
    }

    @Override
    public <R> R accept(TransportVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
