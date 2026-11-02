package transport.domain.tree;

import transport.visitor.TransportVisitor;

import java.util.Objects;

public record PhysicalStationLeaf(String id,
                                  String name,
                                  boolean hasPavilion,
                                  boolean hasWheelchairRamp,
                                  boolean hasTactilePaving) implements TransportElement {

    public PhysicalStationLeaf {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
    }

    @Override
    public <R> R accept(TransportVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
