package transport.visitor;

import org.junit.jupiter.api.Test;
import transport.SampleNetwork;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransportNetworkJsonExportVisitorTest {

    @Test
    @SuppressWarnings("unchecked")
    void exportIsValidJsonWithCorrectNesting() {
        String json = SampleNetwork.network().accept(new TransportNetworkJsonExportVisitor());

        Map<String, Object> network = (Map<String, Object>) MiniJsonParser.parse(json);
        List<Object> routes = (List<Object>) network.get("routes");
        Map<String, Object> busRoute = (Map<String, Object>) routes.get(0);
        List<Object> busRouteElements = (List<Object>) busRoute.get("elements");
        Map<String, Object> firstBus = (Map<String, Object>) busRouteElements.get(0);
        Map<String, Object> marketStation = (Map<String, Object>) busRouteElements.get(4);
        Map<String, Object> tramRoute = (Map<String, Object>) routes.get(1);

        assertEquals("network", network.get("type"));
        assertEquals(2, routes.size());
        assertEquals("R10", busRoute.get("routeId"));
        assertEquals(5, busRouteElements.size());
        assertEquals("B-101", firstBus.get("id"));
        assertEquals(80.0, (Double) firstBus.get("capacity"), 0.0);
        assertEquals("Market \"Square\" \\ East", marketStation.get("name"));
        assertEquals(Boolean.FALSE, marketStation.get("hasWheelchairRamp"));
        assertEquals(4, ((List<Object>) tramRoute.get("elements")).size());
    }
}
