package ee.ctob.api.controller;


import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.data.enums.RoomStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;
import testutils.TestContainer;

import java.util.UUID;

import static ee.ctob.data.enums.RoomStatus.AVAILABLE;
import static ee.ctob.data.enums.RoomStatus.CLOSED;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@Testcontainers
class TestBackofficeController extends TestContainer {


    @Autowired
    BackofficeController controller;

    UUID roomUUID;
    UUID roomValidationUUID;
    UUID conferenceUUID;
    UUID conferenceValidationUUID;

    @Test
    void roomCreate() {
        Request request = createRoomCreateRequest(true);

        Response response = controller.roomCreate(request);
        assertAll(
                ()-> assertNotNull("Response", response),
                ()-> assertNotNull("roomUUID", response.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("reason", response.getReason())
        );
        roomUUID = response.getRoomUUID();
        roomValidationUUID = response.getValidationUUID();
    }

    @Test
    void roomUpdateFail() {
        Request request = createRoomUpdateRequest(true, true, null, null);
        Response response = controller.roomUpdate(request);
        assertAll("assert response of room update",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("roomUUID", response.getRoomUUID()),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("status", response.getRoomStatus()),
                ()-> assertNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("reason", "Please provide new room status OR new capacity", response.getReason())
        );
    }

    @Test
    void roomUpdateStatus() {
        Request request1 = createRoomUpdateRequest(true, false, CLOSED, null);
        Response response1 = controller.roomUpdate(request1);
        assertAll(
                ()-> assertNotNull("Response", response1),
                ()-> assertNotNull("roomUUID", response1.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response1.getValidationUUID()),
                ()-> assertNull("reason", response1.getRoomCapacity()),
                ()-> assertEquals(AVAILABLE, response1.getRoomStatus())
        );

        Request request2 = createRoomUpdateRequest(false, false, null, 5);
        Response response2 = controller.roomUpdate(request2);
        assertAll(
                ()-> assertNotNull("Response", response2),
                ()-> assertNotNull("roomUUID", response2.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response2.getValidationUUID()),
                ()-> assertNull("reason", response2.getRoomCapacity()),
                ()-> assertEquals(AVAILABLE, response2.getRoomStatus())
        );

    }


    private Request createRoomUpdateRequest(boolean forStatus, boolean forFail, RoomStatus newStatus, Integer newCapacity) {
        RoomStatus status = AVAILABLE;
        Integer roomCapacity = 20;
        if(!forFail) {
            if (forStatus) {
                roomCapacity = null;
                status = newStatus;
            } else {
                roomCapacity = newCapacity;
                status = null;
            }
        }
        return new Request(
                null,
                null,
                roomCapacity,
                status,
                null,
                null,
                null,
                null,
                roomValidationUUID,
                null
        );
    }


    private Request createRoomCreateRequest(boolean legit) {
        String roomName = "TestRoom";
        String roomLocation = "Tallinn";
        int roomCapacity = 10;
        String roomDescription = "For tet purpose";

        if(legit) {
            roomLocation = "Tallinn";
        }

        return new Request(
                roomName,
                roomLocation,
                roomCapacity,
                null,
                null,
                roomDescription,
                null,
                null,
                null,
                null
        );
    }
}