package transport.visitor;

import org.junit.jupiter.api.Test;
import transport.SampleNetwork;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmissionAuditVisitorTest {

    private static final double DELTA = 1e-9;

    private final EmissionAuditVisitor visitor = new EmissionAuditVisitor(2.5, 0.5, 4.0);

    /*
     * Hand calculation (diesel 2.5 kg/l, grid 0.5 kg/kWh, tram 4.0 kWh/km):
     *   B-101: 3000 l * 2.5      = 7500
     *   B-102: 24000 kWh * 0.5   = 12000
     *   B-103: 80000 l * 2.5     = 200000
     *   T-301: 20000 kWh * 0.5   = 10000
     *   T-302: 28000 kWh * 0.5   = 14000
     *   R10 = 219500, T3 = 24000, network = 243500
     */
    @Test
    void networkAndRouteFootprintsMatchHandCalculation() {
        assertEquals(243_500.0, SampleNetwork.network().accept(visitor), DELTA);
        assertEquals(219_500.0, SampleNetwork.busRoute().accept(visitor), DELTA);
        assertEquals(24_000.0, SampleNetwork.tramRoute().accept(visitor), DELTA);
    }
}
