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
        Response response = controller.roomCreate(createRoomCreateRequest(true));
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
        roomCreate();
        Response response1 = controller.roomUpdate(createRoomUpdateRequest(null, CLOSED, 50));
        assertAll("assert response1 of room update",
                ()-> assertNotNull("Response", response1),
                ()-> assertNull("roomUUID", response1.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response1.getValidationUUID()),
                ()-> assertNull("status", response1.getRoomStatus()),
                ()-> assertNull("capacity", response1.getRoomCapacity()),
                ()-> assertEquals("reason", "Please provide new room status OR new capacity", response1.getReason())
        );

        Response response2 = controller.roomUpdate(createRoomUpdateRequest(UUID.randomUUID(), CLOSED, null));
        assertAll("assert response2 of room update",
                ()-> assertNotNull("Response", response2),
                ()-> assertNull("roomUUID", response2.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response2.getValidationUUID()),
                ()-> assertNull("status", response2.getRoomStatus()),
                ()-> assertNull("capacity", response2.getRoomCapacity()),
                ()-> assertEquals("reason", "Room not found, check validationUUID", response2.getReason())
        );

        Response response3 = controller.roomUpdate(createRoomUpdateRequest(null, AVAILABLE, null));
        assertAll("assert response3 of room update",
                ()-> assertNotNull("Response", response3),
                ()-> assertNull("roomUUID", response3.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response3.getValidationUUID()),
                ()-> assertNull("status", response3.getRoomStatus()),
                ()-> assertNull("capacity", response3.getRoomCapacity()),
                ()-> assertEquals("reason", "Room status is already : AVAILABLE", response3.getReason())
        );
    }

    @Test
    void roomUpdateStatus() {
        roomCreate();

        Request request1 = createRoomUpdateRequest(null, CLOSED, null);
        Response response1 = controller.roomUpdate(request1);
        assertAll(
                ()-> assertNotNull("Response", response1),
                ()-> assertEquals("roomUUID",roomUUID, response1.getRoomUUID()),
                ()-> assertEquals("validationUUID", roomValidationUUID, response1.getValidationUUID()),
                ()-> assertNull("reason", response1.getReason())
        );

        Request request2 = createRoomUpdateRequest(null, AVAILABLE, null);
        Response response2 = controller.roomUpdate(request2);
        assertAll(
                ()-> assertNotNull("Response", response2),
                ()-> assertEquals("roomUUID",roomUUID, response2.getRoomUUID()),
                ()-> assertEquals("validationUUID", roomValidationUUID, response2.getValidationUUID()),
                ()-> assertNotNull("capacity", response2.getRoomCapacity()),
                ()-> assertEquals("status", AVAILABLE, response2.getRoomStatus()),
                ()-> assertNull("reason", response2.getReason())
        );

        Request request3 = createRoomUpdateRequest(null,null, 40);
        Response response3 = controller.roomUpdate(request3);
        assertAll(
                ()-> assertNotNull("Response", response3),
                ()-> assertNotNull("roomUUID", response3.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response3.getValidationUUID()),
                ()-> assertNotNull("capacity", response3.getRoomCapacity()),
                ()-> assertEquals("status", AVAILABLE, response3.getRoomStatus()),
                ()-> assertNull("reason", response3.getReason())
        );
    }


    private Request createRoomUpdateRequest(UUID failUUID, RoomStatus status, Integer capacity) {
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