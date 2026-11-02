package transport.domain.tree;

import transport.visitor.TransportVisitor;

public interface TransportElement {

    <R> R accept(TransportVisitor<R> visitor);
}
