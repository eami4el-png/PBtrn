package transport.domain.event;

public enum DelaySeverity {
    MINOR_DELAY,
    CRITICAL_DELAY,
    ACCIDENT;

    public boolean requiresInspection() {
        return this != MINOR_DELAY;
    }
}
