package transport.domain.tree;

import transport.visitor.TransportVisitor;

import java.util.Objects;

public record ChargingHubLeaf(String id,
                              double availablePowerKw,
                              double peakLoadKw) implements TransportElement {

    public ChargingHubLeaf {
        Objects.requireNonNull(id);
    }

    @Override
    public <R> R accept(TransportVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
