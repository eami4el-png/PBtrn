package transport.observer.listeners;

import transport.domain.event.DelayDetectedEvent;
import transport.domain.event.TransportEvent;
import transport.observer.Observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Turns delay events into push notification texts for the passenger app. */
public final class PassengerMobileAppNotifier implements Observer<TransportEvent> {

    private final List<String> sentNotifications = new CopyOnWriteArrayList<>();

    @Override
    public void onUpdate(TransportEvent event) {
        if (event instanceof DelayDetectedEvent delay) {
            sentNotifications.add(String.format("Route %s: vehicle %s is delayed by %d min (%s)",
                    delay.routeId(), delay.vehicleId(), delay.delayMinutes(), delay.severity()));
        }
    }

    public List<String> sentNotifications() {
        return List.copyOf(sentNotifications);
    }
}
