package transport.observer;

import org.junit.jupiter.api.Test;
import transport.TestEvents;
import transport.domain.event.DelaySeverity;
import transport.domain.event.EventType;
import transport.domain.event.TransportEvent;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseSubjectTest {

    private final BaseSubject<TransportEvent> subject = new BaseSubject<>();

    @Test
    void subscribedObserverReceivesEventOfItsType() {
        List<TransportEvent> received = new ArrayList<>();
        subject.subscribe(EventType.VEHICLE_LOCATION, received::add);

        subject.notify(TestEvents.location("V-1"));

        assertEquals(List.of(TestEvents.location("V-1")), received);
    }

    @Test
    void unsubscribedObserverReceivesNothing() {
        List<TransportEvent> received = new ArrayList<>();
        Observer<TransportEvent> observer = received::add;
        subject.subscribe(EventType.VEHICLE_LOCATION, observer);
        subject.unsubscribe(EventType.VEHICLE_LOCATION, observer);

        subject.notify(TestEvents.location("V-1"));

        assertEquals(0, received.size());
    }

    @Test
    void observerDoesNotReceiveEventsOfOtherTypes() {
        List<TransportEvent> delays = new ArrayList<>();
        List<TransportEvent> locations = new ArrayList<>();
        subject.subscribe(EventType.DELAY_DETECTED, delays::add);
        subject.subscribe(EventType.VEHICLE_LOCATION, locations::add);

        subject.notify(TestEvents.delay("V-1", DelaySeverity.CRITICAL_DELAY));

        assertEquals(1, delays.size());
        assertEquals(0, locations.size());
    }

    @Test
    void observerMayUnsubscribeItselfInsideOnUpdate() {
        List<TransportEvent> oneShotReceived = new ArrayList<>();
        List<TransportEvent> permanentReceived = new ArrayList<>();
        Observer<TransportEvent> oneShot = new Observer<>() {
            @Override
            public void onUpdate(TransportEvent event) {
                oneShotReceived.add(event);
                subject.unsubscribe(EventType.VEHICLE_LOCATION, this);
            }
        };
        subject.subscribe(EventType.VEHICLE_LOCATION, oneShot);
        subject.subscribe(EventType.VEHICLE_LOCATION, permanentReceived::add);

        subject.notify(TestEvents.location("V-1"));
        subject.notify(TestEvents.location("V-2"));

        assertEquals(1, oneShotReceived.size());
        assertEquals(2, permanentReceived.size());
    }
}
