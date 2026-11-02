package transport.observer.listeners;

import transport.domain.event.TransportEvent;
import transport.domain.event.VehicleLocationEvent;
import transport.observer.Observer;

import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

/** Keeps the latest estimated time of arrival (minutes) per vehicle. */
public final class StopDisplayBoard implements Observer<TransportEvent> {

    private static final double MINUTES_PER_HOUR = 60.0;

    private final Map<String, Integer> etaMinutesByVehicle = new ConcurrentHashMap<>();

    @Override
    public void onUpdate(TransportEvent event) {
        if (event instanceof VehicleLocationEvent location) {
            updateEta(location);
        }
    }

    public OptionalInt etaMinutes(String vehicleId) {
        Integer eta = etaMinutesByVehicle.get(vehicleId);
        return eta == null ? OptionalInt.empty() : OptionalInt.of(eta);
    }

    private void updateEta(VehicleLocationEvent location) {
        if (location.speedKmh() <= 0) {
            etaMinutesByVehicle.remove(location.vehicleId());
            return;
        }
        double minutes = location.distanceToNextStopKm() * MINUTES_PER_HOUR / location.speedKmh();
        etaMinutesByVehicle.put(location.vehicleId(), (int) Math.ceil(minutes));
    }
}
