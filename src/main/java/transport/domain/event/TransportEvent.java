package transport.domain.event;

import java.time.Instant;

public sealed interface TransportEvent
        permits VehicleLocationEvent, DelayDetectedEvent, PassengerBoardingEvent,
                HourlyTrafficReportEvent, MaintenanceTicket {

    EventType type();

    Instant timestamp();
}
