package transport.visitor;

import org.junit.jupiter.api.Test;
import transport.SampleNetwork;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FleetOperatingCostVisitorTest {

    private static final double DELTA = 1e-9;

    private final FleetOperatingCostVisitor visitor =
            new FleetOperatingCostVisitor(new OperatingCostRates(2.0, 0.5, 4.0, 0.1));

    /*
     * Hand calculation (fuel 2.0/l, electricity 0.5/kWh, tram 4.0 kWh/km, maintenance 0.1/km):
     *   B-101 diesel:   10000/100*30 = 3000 l   -> 6000   + 1000  = 7000
     *   B-102 electric: 20000/100*120 = 24000 kWh -> 12000 + 2000  = 14000
     *   B-103 diesel:   320000/100*25 = 80000 l  -> 160000 + 32000 = 192000
     *   T-301:          5000*4 = 20000 kWh      -> 10000  + 500   = 10500
     *   T-302:          7000*4 = 28000 kWh      -> 14000  + 700   = 14700
     *   R10 = 213000, T3 = 25200, network = 238200
     */
    @Test
    void networkCostMatchesHandCalculation() {
        assertEquals(238_200.0, SampleNetwork.network().accept(visitor), DELTA);
    }

    @Test
    void subtreeCostCountsOnlyThatRoute() {
        assertEquals(213_000.0, SampleNetwork.busRoute().accept(visitor), DELTA);
        assertEquals(25_200.0, SampleNetwork.tramRoute().accept(visitor), DELTA);
    }
}
