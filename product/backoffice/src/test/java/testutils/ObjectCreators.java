package testutils;

import ee.ctob.api.Request;
import ee.ctob.api.enums.RoomStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ObjectCreators {
    public static Request createRoomCreateRequest() {
        return new Request(
                "TestRoom",
                "Tallinn",
                100,
                null,
                null,
                "For tet purpose",
                null,
                null,
                null
        );
    }

    public static Request createRoomUpdateRequest(UUID roomValidationUUID, UUID failUUID, RoomStatus status, Integer capacity) {
        UUID validationUUUID = roomValidationUUID;
        if(failUUID != null) {
            validationUUUID = failUUID;
        }
        return new Request(
                null,
                null,
                capacity,
                status,
                null,
                null,
                null,
                null,
                validationUUUID
        );
    }

    public static Request createConferenceRequest(String from, String until, UUID validationUUID, UUID rUUID) {
        return new  Request(
                null,
                null,
                null,
                null,
                rUUID,
                "Some info as example",
                LocalDateTime.parse(from),
                LocalDateTime.parse(until),
                validationUUID
        );
    }

    public static Request createConferenceUUIDRequest(UUID validationUUID) {
        return new  Request(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                validationUUID
        );
    }
}
