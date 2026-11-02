package transport.visitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Strict minimal JSON parser used only to prove that the exported document is valid. */
final class MiniJsonParser {

    private final String text;
    private int position;

    private MiniJsonParser(String text) {
        this.text = text;
    }

    static Object parse(String text) {
        MiniJsonParser parser = new MiniJsonParser(text);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.position != text.length()) {
            throw new IllegalArgumentException("Trailing characters at " + parser.position);
        }
        return value;
    }

    private Object readValue() {
        skipWhitespace();
        char c = peek();
        if (c == '{') {
            return readObject();
        }
        if (c == '[') {
            return readArray();
        }
        if (c == '"') {
            return readString();
        }
        if (text.startsWith("true", position)) {
            position += 4;
            return Boolean.TRUE;
        }
        if (text.startsWith("false", position)) {
            position += 5;
            return Boolean.FALSE;
        }
        return readNumber();
    }

    private Map<String, Object> readObject() {
        Map<String, Object> object = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();
        if (peek() == '}') {
            position++;
            return object;
        }
        do {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            object.put(key, readValue());
            skipWhitespace();
        } while (consumeIf(','));
        expect('}');
        return object;
    }

    private List<Object> readArray() {
        List<Object> array = new ArrayList<>();
        expect('[');
        skipWhitespace();
        if (peek() == ']') {
            position++;
            return array;
        }
        do {
            array.add(readValue());
            skipWhitespace();
        } while (consumeIf(','));
        expect(']');
        return array;
    }

    private String readString() {
        expect('"');
        StringBuilder value = new StringBuilder();
        while (peek() != '"') {
            char c = text.charAt(position++);
            if (c == '\\') {
                char escaped = text.charAt(position++);
                switch (escaped) {
                    case '"', '\\', '/' -> value.append(escaped);
                    case 'n' -> value.append('\n');
                    case 'r' -> value.append('\r');
                    case 't' -> value.append('\t');
                    case 'u' -> {
                        value.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                        position += 4;
                    }
                    default -> throw new IllegalArgumentException("Bad escape at " + position);
                }
            } else if (c < 0x20) {
                throw new IllegalArgumentException("Unescaped control character at " + position);
            } else {
                value.append(c);
            }
        }
        position++;
        return value.toString();
    }

    private Double readNumber() {
        int start = position;
        while (position < text.length() && "+-0123456789.eE".indexOf(text.charAt(position)) >= 0) {
            position++;
        }
        if (start == position) {
            throw new IllegalArgumentException("Unexpected character at " + position);
        }
        return Double.valueOf(text.substring(start, position));
    }

    private char peek() {
        if (position >= text.length()) {
            throw new IllegalArgumentException("Unexpected end of input");
        }
        return text.charAt(position);
    }

    private void expect(char expected) {
        if (peek() != expected) {
            throw new IllegalArgumentException("Expected '" + expected + "' at " + position);
        }
        position++;
    }

    private boolean consumeIf(char expected) {
        if (position < text.length() && text.charAt(position) == expected) {
            position++;
            return true;
        }
        return false;
    }

    private void skipWhitespace() {
        while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
            position++;
        }
    }
}
