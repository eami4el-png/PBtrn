package transport.observer;

import transport.domain.event.EventType;
import transport.domain.event.TransportEvent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe listener registry. No lock guards the subject as a whole:
 * the map is concurrent and every per-type list is copy-on-write, so
 * notify() iterates a stable snapshot and observers may subscribe or
 * unsubscribe (even from inside onUpdate) without breaking delivery.
 */
public class BaseSubject<E extends TransportEvent> implements Subject<E> {

    private final Map<EventType, CopyOnWriteArrayList<Observer<E>>> observersByType =
            new ConcurrentHashMap<>();

    @Override
    public void subscribe(EventType type, Observer<E> observer) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(observer);
        observersByType
                .computeIfAbsent(type, key -> new CopyOnWriteArrayList<>())
                .addIfAbsent(observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<E> observer) {
        CopyOnWriteArrayList<Observer<E>> observers = observersByType.get(type);
        if (observers != null) {
            observers.remove(observer);
        }
    }

    @Override
    public void notify(E event) {
        CopyOnWriteArrayList<Observer<E>> observers = observersByType.get(event.type());
        if (observers == null) {
            return;
        }
        for (Observer<E> observer : observers) {
            observer.onUpdate(event);
        }
    }
}
