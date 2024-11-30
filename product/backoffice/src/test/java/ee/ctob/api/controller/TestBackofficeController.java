package ee.ctob.api.controller;


import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.data.enums.RoomStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;
import testutils.TestContainer;

import java.time.LocalDateTime;
import java.util.UUID;

import static ee.ctob.data.enums.RoomStatus.AVAILABLE;
import static ee.ctob.data.enums.RoomStatus.CLOSED;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@Testcontainers
class TestBackofficeController extends TestContainer {


    @Autowired
    private BackofficeController controller;

    private UUID roomUUID;
    private UUID roomValidationUUID;
    private UUID conferenceUUID;
    private UUID conferenceValidationUUID;
    private Request request;
    private Response response;

    @Test
    void roomCreate() {
        createRoomCreateRequest(true);
        response = controller.roomCreate(request);

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

        createRoomUpdateRequest(null, CLOSED, 50);
        response = controller.roomUpdate(request);
        assertAll("assert response1 of room update",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("roomUUID", response.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("status", response.getRoomStatus()),
                ()-> assertNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("reason", "Please provide new room status OR new capacity", response.getReason())
        );

        createRoomUpdateRequest(UUID.randomUUID(), CLOSED, null);
        response = controller.roomUpdate(request);
        assertAll("assert response2 of room update",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("roomUUID", response.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("status", response.getRoomStatus()),
                ()-> assertNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("reason", "Room not found, check validationUUID", response.getReason())
        );

        createRoomUpdateRequest(null, AVAILABLE, null);
        response = controller.roomUpdate(request);
        assertAll("assert response3 of room update",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("roomUUID", response.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("status", response.getRoomStatus()),
                ()-> assertNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("reason", "Room status is already : AVAILABLE", response.getReason())
        );
    }

    @Test
    void roomUpdateStatus() {
        roomCreate();

        createRoomUpdateRequest(null, CLOSED, null);
        response = controller.roomUpdate(request);
        assertAll(
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("roomUUID",roomUUID, response.getRoomUUID()),
                ()-> assertEquals("validationUUID", roomValidationUUID, response.getValidationUUID()),
                ()-> assertNull("reason", response.getReason())
        );

        createRoomUpdateRequest(null, AVAILABLE, null);
        response = controller.roomUpdate(request);
        assertAll(
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("roomUUID",roomUUID, response.getRoomUUID()),
                ()-> assertEquals("validationUUID", roomValidationUUID, response.getValidationUUID()),
                ()-> assertNotNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("status", AVAILABLE, response.getRoomStatus()),
                ()-> assertNull("reason", response.getReason())
        );

        createRoomUpdateRequest(null,null, 40);
        response= controller.roomUpdate(request);
        assertAll(
                ()-> assertNotNull("Response", response),
                ()-> assertNotNull("roomUUID", response.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNotNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("status", AVAILABLE, response.getRoomStatus()),
                ()-> assertNull("reason", response.getReason())
        );
    }

    @Test
    void conferenceCreate() {
        roomCreate();

        createConferenceCreateRequest("2024-12-28T10:00:00", "2024-12-28T15:00:00");
        response = controller.conferenceCreate(request);
        assertAll(
                ()-> assertNotNull("Response", response),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNotNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNotNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNotNull("bookedUntil", response.getBookedUntil()),
                ()-> assertNull("reason", response.getReason())
        );

        createConferenceCreateRequest("2024-12-31T10:00:00", "2024-12-31T15:00:00");
        response = controller.conferenceCreate(request);
        assertAll(
                ()-> assertNotNull("Response", response),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNotNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNotNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNotNull("bookedUntil", response.getBookedUntil()),
                ()-> assertNull("reason", response.getReason())
        );
        conferenceUUID = response.getConferenceUUID();
        conferenceValidationUUID = response.getValidationUUID();
    }

    @Test
    void conferenceCreateFail() {
        roomCreate();
        conferenceCreate();

        createConferenceCreateRequest("2024-12-31T10:00:00", "2024-12-31T15:00:00");
        response = controller.conferenceCreate(request);
        assertAll(
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequest("2024-12-31T09:00:00", "2024-12-31T16:00:00");
        response = controller.conferenceCreate(request);
        assertAll(
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequest("2024-12-31T09:00:00", "2024-12-31T10:00:01");
        response = controller.conferenceCreate(request);
        assertAll(
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequest("2024-12-31T14:59:59", "2024-12-31T16:00:00");
        response = controller.conferenceCreate(request);
        assertAll(
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

    }

    private void createConferenceCreateRequest(String from, String until) {
        request = new  Request(
                null,
                null,
                null,
                null,
                roomUUID,
                "Some info as example",
                LocalDateTime.parse(from),
                LocalDateTime.parse(until),
                null,
                null
        );
    }

    private void createRoomUpdateRequest(UUID failUUID, RoomStatus status, Integer capacity) {
        UUID validationUUUID = roomValidationUUID;
        if(failUUID != null) {
            validationUUUID = failUUID;
        }
        request = new Request(
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


    private void createRoomCreateRequest(boolean legit) {
        String roomName = "TestRoom";
        String roomLocation = "Tallinn";
        int roomCapacity = 10;
        String roomDescription = "For tet purpose";

        if(legit) {
            roomLocation = "Tallinn";
        }

        request = new  Request(
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