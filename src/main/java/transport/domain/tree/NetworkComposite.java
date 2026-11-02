package transport.domain.tree;

import transport.visitor.TransportVisitor;

import java.util.List;
import java.util.Objects;

public record NetworkComposite(String id, String name, List<RouteComposite> routes)
        implements TransportElement {

    public NetworkComposite {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        routes = List.copyOf(routes);
    }

    @Override
    public <R> R accept(TransportVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
