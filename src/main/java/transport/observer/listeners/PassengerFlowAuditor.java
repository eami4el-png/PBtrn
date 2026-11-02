package transport.observer.listeners;

import transport.domain.event.PassengerBoardingEvent;
import transport.observer.Observer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toUnmodifiableSet;

/** Counts boardings per station and reports stations that reached the crowding threshold. */
public final class PassengerFlowAuditor implements Observer<PassengerBoardingEvent> {

    private final int crowdingThreshold;
    private final Map<String, Integer> passengersByStation = new ConcurrentHashMap<>();

    public PassengerFlowAuditor(int crowdingThreshold) {
        this.crowdingThreshold = crowdingThreshold;
    }

    @Override
    public void onUpdate(PassengerBoardingEvent event) {
        passengersByStation.merge(event.stationId(), event.passengers(), Integer::sum);
    }

    public int passengersAt(String stationId) {
        return passengersByStation.getOrDefault(stationId, 0);
    }

    public Set<String> overcrowdedStations() {
        return passengersByStation.entrySet().stream()
                .filter(station -> station.getValue() >= crowdingThreshold)
                .map(Map.Entry::getKey)
                .collect(toUnmodifiableSet());
    }
}
