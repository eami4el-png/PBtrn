package transport.visitor;

import org.junit.jupiter.api.Test;
import transport.SampleNetwork;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccessibilityAuditVisitorTest {

    @Test
    void returnsOnlyStationsMissingRampOrTactilePaving() {
        var failing = SampleNetwork.network().accept(new AccessibilityAuditVisitor());

        assertEquals(List.of(SampleNetwork.MARKET_STATION, SampleNetwork.DEPOT_STATION), failing);
    }
}
