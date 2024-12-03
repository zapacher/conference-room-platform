package testutils;

import ee.ctob.api.Request;
import ee.ctob.data.enums.RoomStatus;

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
                validationUUUID,
                null
        );
    }

    public static Request createConferenceCreateRequest(String from, String until, UUID rUUID, UUID validationUUID) {
        return createConferenceRequest(null, from, until, validationUUID, rUUID);
    }
    public static Request createConferenceUpdateWithRoomRequest(String from, String until, UUID newRoomUUID, UUID validationUUID) {
        return createConferenceRequest(newRoomUUID, from, until, validationUUID, null);
    }

    public static Request createConferenceRequest(UUID newRoomUUID, String from, String until, UUID validationUUID, UUID rUUID) {
        return new  Request(
                null,
                null,
                null,
                null,
                rUUID,
                "Some info as example",
                LocalDateTime.parse(from),
                LocalDateTime.parse(until),
                validationUUID,
                newRoomUUID
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
                validationUUID,
                null
        );
    }
}
