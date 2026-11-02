package transport.visitor;

import org.junit.jupiter.api.Test;
import transport.SampleNetwork;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetworkCensusVisitorTest {

    @Test
    void censusAccumulatesAcrossWholeTree() {
        NetworkCensus census = SampleNetwork.network().accept(new NetworkCensusVisitor());

        assertEquals(5, census.vehicleCount());
        assertEquals(540, census.totalCapacity());
        assertEquals(62.5, census.averagePantographWearPercent(), 1e-9);
    }
}
