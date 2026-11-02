package transport.service;

import transport.domain.event.DelayDetectedEvent;
import transport.domain.event.MaintenanceTicket;
import transport.domain.event.TransportEvent;
import transport.domain.tree.NetworkComposite;
import transport.domain.tree.RouteComposite;
import transport.observer.BaseSubject;
import transport.observer.Observer;
import transport.visitor.RouteEmergencyInspectionVisitor;

import java.util.Objects;
import java.util.Optional;

/**
 * Bridge from Observer to Visitor: a serious delay selects the affected RouteComposite,
 * the inspection visitor runs on that subtree only, and the resulting MaintenanceTicket
 * is published again to this service's own subscribers.
 */
public final class DelayAlertService extends BaseSubject<MaintenanceTicket>
        implements Observer<TransportEvent> {

    private final NetworkComposite network;

    public DelayAlertService(NetworkComposite network) {
        this.network = Objects.requireNonNull(network);
    }

    @Override
    public void onUpdate(TransportEvent event) {
        if (event instanceof DelayDetectedEvent delay && delay.severity().requiresInspection()) {
            findRoute(delay.routeId()).ifPresent(route -> inspect(route, delay));
        }
    }

    private Optional<RouteComposite> findRoute(String routeId) {
        return network.routes().stream()
                .filter(route -> route.routeId().equals(routeId))
                .findFirst();
    }

    private void inspect(RouteComposite route, DelayDetectedEvent delay) {
        var inspection = new RouteEmergencyInspectionVisitor(route.routeId(), delay.severity(), delay.timestamp());
        notify(route.accept(inspection));
    }
}
