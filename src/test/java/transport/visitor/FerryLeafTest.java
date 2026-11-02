package transport.visitor;

import org.junit.jupiter.api.Test;
import transport.domain.tree.FerryLeaf;
import transport.domain.tree.RouteComposite;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FerryLeafTest {

    private static final double DELTA = 1e-9;

    private final RouteComposite ferryRoute = new RouteComposite("F1", "River line",
            List.of(new FerryLeaf("F-1", 200, 100.0, 8_000)));

    /* F-1: 8000/100*100 = 8000 l -> fuel 16000 + maintenance 800 = 16800; emissions 8000 * 2.5 = 20000 kg. */
    @Test
    void existingVisitorsHandleTheNewElement() {
        double cost = ferryRoute.accept(new FleetOperatingCostVisitor(new OperatingCostRates(2.0, 0.5, 4.0, 0.1)));
        double emissions = ferryRoute.accept(new EmissionAuditVisitor(2.5, 0.5, 4.0));
        NetworkCensus census = ferryRoute.accept(new NetworkCensusVisitor());

        assertEquals(16_800.0, cost, DELTA);
        assertEquals(20_000.0, emissions, DELTA);
        assertEquals(1, census.vehicleCount());
        assertEquals(200, census.totalCapacity());
    }

    @Test
    @SuppressWarnings("unchecked")
    void jsonExportContainsFerryNode() {
        String json = ferryRoute.accept(new TransportNetworkJsonExportVisitor());

        Map<String, Object> route = (Map<String, Object>) MiniJsonParser.parse(json);
        Map<String, Object> ferry = (Map<String, Object>) ((List<Object>) route.get("elements")).get(0);

        assertEquals("ferry", ferry.get("type"));
        assertEquals("F-1", ferry.get("id"));
    }
}
