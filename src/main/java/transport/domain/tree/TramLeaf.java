package transport.domain.tree;

import transport.visitor.TransportVisitor;

import java.util.Objects;

public record TramLeaf(String id,
                       int capacity,
                       double networkVoltage,
                       double pantographWearPercent,
                       double mileageKm) implements TransportElement {

    public TramLeaf {
        Objects.requireNonNull(id);
    }

    @Override
    public <R> R accept(TransportVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
