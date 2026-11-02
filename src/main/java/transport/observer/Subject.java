package transport.observer;

import transport.domain.event.EventType;
import transport.domain.event.TransportEvent;

public interface Subject<E extends TransportEvent> {

    void subscribe(EventType type, Observer<E> observer);

    void unsubscribe(EventType type, Observer<E> observer);

    void notify(E event);
}
