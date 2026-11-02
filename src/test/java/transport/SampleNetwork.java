package transport;

import transport.domain.tree.BusLeaf;
import transport.domain.tree.ChargingHubLeaf;
import transport.domain.tree.EngineType;
import transport.domain.tree.NetworkComposite;
import transport.domain.tree.PhysicalStationLeaf;
import transport.domain.tree.RouteComposite;
import transport.domain.tree.TramLeaf;

import java.util.List;

/** Fixture whose expected visitor results are calculated by hand in the tests. */
public final class SampleNetwork {

    public static final PhysicalStationLeaf MARKET_STATION =
            new PhysicalStationLeaf("S-2", "Market \"Square\" \\ East", true, false, true);
    public static final PhysicalStationLeaf DEPOT_STATION =
            new PhysicalStationLeaf("S-3", "Depot", false, false, false);

    private SampleNetwork() {
    }

    public static RouteComposite busRoute() {
        return new RouteComposite("R10", "Bus route 10", List.of(
                new BusLeaf("B-101", 80, EngineType.DIESEL, 30.0, 10_000),
                new BusLeaf("B-102", 100, EngineType.ELECTRIC, 120.0, 20_000),
                new BusLeaf("B-103", 60, EngineType.DIESEL, 25.0, 320_000),
                new PhysicalStationLeaf("S-1", "Central", true, true, true),
                MARKET_STATION));
    }

    public static RouteComposite tramRoute() {
        return new RouteComposite("T3", "Tram route 3", List.of(
                new TramLeaf("T-301", 150, 600.0, 40.0, 5_000),
                new TramLeaf("T-302", 150, 600.0, 85.0, 7_000),
                DEPOT_STATION,
                new ChargingHubLeaf("H-1", 500.0, 480.0)));
    }

    public static NetworkComposite network() {
        return new NetworkComposite("NET-1", "City transport network", List.of(busRoute(), tramRoute()));
    }
}
