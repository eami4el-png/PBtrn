package transport.domain.tree;

import transport.visitor.TransportVisitor;

import java.util.List;
import java.util.Objects;

public record RouteComposite(String routeId, String name, List<TransportElement> elements)
        implements TransportElement {

    public RouteComposite {
        Objects.requireNonNull(routeId);
        Objects.requireNonNull(name);
        elements = List.copyOf(elements);
    }

    @Override
    public <R> R accept(TransportVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
