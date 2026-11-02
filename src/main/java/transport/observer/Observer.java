package transport.observer;

import transport.domain.event.TransportEvent;

@FunctionalInterface
public interface Observer<E extends TransportEvent> {

    void onUpdate(E event);
}
