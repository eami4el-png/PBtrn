package transport.visitor;

import transport.domain.tree.BusLeaf;
import transport.domain.tree.ChargingHubLeaf;
import transport.domain.tree.FerryLeaf;
import transport.domain.tree.NetworkComposite;
import transport.domain.tree.PhysicalStationLeaf;
import transport.domain.tree.RouteComposite;
import transport.domain.tree.TramLeaf;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * Hand-written JSON export. Every visit returns the node as a multi-line block
 * that starts at column 0; parents indent the blocks of their children.
 */
public final class TransportNetworkJsonExportVisitor implements TransportVisitor<String> {

    private static final String INDENT = "  ";

    @Override
    public String visit(NetworkComposite network) {
        return object(fields(
                "type", quote("network"),
                "id", quote(network.id()),
                "name", quote(network.name()),
                "routes", array(network.routes().stream().map(route -> route.accept(this)).toList())));
    }

    @Override
    public String visit(RouteComposite route) {
        return object(fields(
                "type", quote("route"),
                "routeId", quote(route.routeId()),
                "name", quote(route.name()),
                "elements", array(route.elements().stream().map(element -> element.accept(this)).toList())));
    }

    @Override
    public String visit(BusLeaf bus) {
        return object(fields(
                "type", quote("bus"),
                "id", quote(bus.id()),
                "capacity", String.valueOf(bus.capacity()),
                "engineType", quote(bus.engineType().name()),
                "fuelPer100Km", number(bus.fuelPer100Km()),
                "mileageKm", number(bus.mileageKm())));
    }

    @Override
    public String visit(TramLeaf tram) {
        return object(fields(
                "type", quote("tram"),
                "id", quote(tram.id()),
                "capacity", String.valueOf(tram.capacity()),
                "networkVoltage", number(tram.networkVoltage()),
                "pantographWearPercent", number(tram.pantographWearPercent()),
                "mileageKm", number(tram.mileageKm())));
    }

    @Override
    public String visit(FerryLeaf ferry) {
        return object(fields(
                "type", quote("ferry"),
                "id", quote(ferry.id()),
                "capacity", String.valueOf(ferry.capacity()),
                "fuelPer100Km", number(ferry.fuelPer100Km()),
                "mileageKm", number(ferry.mileageKm())));
    }

    @Override
    public String visit(PhysicalStationLeaf station) {
        return object(fields(
                "type", quote("station"),
                "id", quote(station.id()),
                "name", quote(station.name()),
                "hasPavilion", String.valueOf(station.hasPavilion()),
                "hasWheelchairRamp", String.valueOf(station.hasWheelchairRamp()),
                "hasTactilePaving", String.valueOf(station.hasTactilePaving())));
    }

    @Override
    public String visit(ChargingHubLeaf chargingHub) {
        return object(fields(
                "type", quote("chargingHub"),
                "id", quote(chargingHub.id()),
                "availablePowerKw", number(chargingHub.availablePowerKw()),
                "peakLoadKw", number(chargingHub.peakLoadKw())));
    }

    private static Map<String, String> fields(String... keysAndRenderedValues) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (int i = 0; i < keysAndRenderedValues.length; i += 2) {
            fields.put(keysAndRenderedValues[i], keysAndRenderedValues[i + 1]);
        }
        return fields;
    }

    private static String object(Map<String, String> fields) {
        String body = fields.entrySet().stream()
                .map(field -> INDENT + quote(field.getKey()) + ": " + indentTail(field.getValue()))
                .collect(joining(",\n"));
        return "{\n" + body + "\n}";
    }

    private static String array(List<String> renderedItems) {
        if (renderedItems.isEmpty()) {
            return "[]";
        }
        String body = renderedItems.stream()
                .map(item -> INDENT + indentTail(item))
                .collect(joining(",\n"));
        return "[\n" + body + "\n]";
    }

    private static String indentTail(String block) {
        return block.replace("\n", "\n" + INDENT);
    }

    private static String number(double value) {
        return BigDecimal.valueOf(value).toPlainString();
    }

    private static String quote(String text) {
        StringBuilder quoted = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> quoted.append("\\\"");
                case '\\' -> quoted.append("\\\\");
                case '\n' -> quoted.append("\\n");
                case '\r' -> quoted.append("\\r");
                case '\t' -> quoted.append("\\t");
                default -> {
                    if (c < 0x20) {
                        quoted.append(String.format("\\u%04x", (int) c));
                    } else {
                        quoted.append(c);
                    }
                }
            }
        }
        return quoted.append('"').toString();
    }
}
