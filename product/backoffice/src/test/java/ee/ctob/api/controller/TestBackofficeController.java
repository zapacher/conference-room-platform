package ee.ctob.api.controller;


import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.data.Conference;
import ee.ctob.data.enums.RoomStatus;
import org.junit.jupiter.api.Test;
import org.mockito.internal.creation.SuspendMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;
import testutils.TestContainer;

import java.time.LocalDateTime;
import java.util.Optional;
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
    private boolean withoutRoom = false;
    @Test
    void roomCreate() {
        createRoomCreateRequest(true);
        response = controller.roomCreate(request);

        assertAll( "Room create success",
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
        assertAll("Room update fail, double update",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("roomUUID", response.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("status", response.getRoomStatus()),
                ()-> assertNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("reason", "Please provide new room status OR new capacity", response.getReason())
        );

        createRoomUpdateRequest(UUID.randomUUID(), CLOSED, null);
        response = controller.roomUpdate(request);
        assertAll("Room update fail, validation uuid not valid",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("roomUUID", response.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("status", response.getRoomStatus()),
                ()-> assertNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("reason", "Room not found, check validationUUID", response.getReason())
        );

        createRoomUpdateRequest(null, AVAILABLE, null);
        response = controller.roomUpdate(request);
        assertAll("Room update fail, status is the same",
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
        assertAll("Room update success status",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("roomUUID", roomUUID, response.getRoomUUID()),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertNotNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("status", request.getStatus(), response.getRoomStatus()),
                ()-> assertNull("reason", response.getReason())
        );

        createRoomUpdateRequest(null, AVAILABLE, null);
        response = controller.roomUpdate(request);
        assertAll("Room update success status",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("roomUUID", roomUUID, response.getRoomUUID()),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertNotNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("status", AVAILABLE, response.getRoomStatus()),
                ()-> assertNull("reason", response.getReason())
        );

        createRoomUpdateRequest(null,null, 40);
        response= controller.roomUpdate(request);
        assertAll("Room update success capacity",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("roomUUID", roomUUID, response.getRoomUUID()),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertEquals("capacity", request.getCapacity(), response.getRoomCapacity()),
                ()-> assertEquals("status", AVAILABLE, response.getRoomStatus()),
                ()-> assertNull("reason", response.getReason())
        );
    }

    @Test
    void conferenceCreate() {
        if(!withoutRoom) {
            roomCreate();
        }

        createConferenceCreateRequest("2024-12-28T10:00:00", "2024-12-28T15:00:00");
        response = controller.conferenceCreate(request);
        assertAll("Conference create success",
                ()-> assertNotNull("Response", response),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNotNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertEquals("bookedFrom", request.getFrom(), response.getBookedFrom()),
                ()-> assertEquals("bookedUntil", request.getUntil(), response.getBookedUntil()),
                ()-> assertNull("reason", response.getReason())
        );

        createConferenceCreateRequest("2024-12-31T10:00:00", "2024-12-31T15:00:00");
        response = controller.conferenceCreate(request);
        assertAll("Conference create success with other time",
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
        assertAll("create conference fail overlapping time",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequest("2024-12-31T09:00:00", "2024-12-31T16:00:00");
        response = controller.conferenceCreate(request);
        assertAll("create conference fail overlapping time",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequest("2024-12-31T09:00:00", "2024-12-31T10:00:01");
        response = controller.conferenceCreate(request);
        assertAll("create conference fail overlapping time",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequest("2024-12-31T14:59:59", "2024-12-31T16:00:00");
        response = controller.conferenceCreate(request);
        assertAll("create conference fail overlapping time",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequest("2024-12-31T14:59:59", "2024-12-31T16:00:00");
        response = controller.conferenceCreate(request);
        assertAll("create conference fail overlapping time",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequest("2024-12-20T10:00:00", "2024-12-20T08:00:00", UUID.randomUUID());
        response = controller.conferenceCreate(request);
        assertAll("create conference fail roomUUID not valid",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen room isn't available", response.getReason())
        );
    }

    @Test
    void conferenceCancel() {
        roomCreate();
        conferenceCreate();
        createConferenceUUIDRequest(null);
        response = controller.conferenceCancel(request);
        assertAll("Conference cancel success",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertNull("reason", response.getReason())
        );
    }

    @Test
    void conferenceCancelFail() {
        roomCreate();
        conferenceCreate();

        createConferenceUUIDRequest(UUID.randomUUID());
        response = controller.conferenceCancel(request);
        assertAll("Conference cancel fail, uuid not valid",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Conference is already canceled or not exists", response.getReason())
        );

        conferenceCancel();
        createConferenceUUIDRequest(null);
        response = controller.conferenceCancel(request);
        assertAll("conference cancel fail, already canceled",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Conference is already canceled or not exists", response.getReason())
        );
    }

    @Test
    void conferenceFeedbackFail() {
        roomCreate();
        conferenceCreate();

        createConferenceUUIDRequest(null);
        response = controller.conferenceFeedbacks(request);
        assertAll("conference feedbacks fail, no participants",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Conference has no participants", response.getReason())
        );

        createConferenceUUIDRequest(UUID.randomUUID());
        response = controller.conferenceFeedbacks(request);
        assertAll("conference cancel fail, already canceled",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Conference doesn't exists", response.getReason())
        );
    }

    @Test
    void conferenceSpace() {
        roomCreate();
        conferenceCreate();

        createConferenceUUIDRequest(null);
        response = controller.conferenceSpace(request);
        System.out.println(response);
        assertAll("Conference space success",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertEquals("availableSpace", (Integer) 100, response.getAvailableSpace()),
                ()-> assertEquals("rooCapacity", (Integer) 100, response.getRoomCapacity()),
                ()-> assertEquals("participantCount", (Integer) 0, response.getParticipantsCount()),
                ()-> assertNull("reason", response.getReason())
        );

        createConferenceUUIDRequest(UUID.randomUUID());
        response = controller.conferenceSpace(request);
        System.out.println(response);
        assertAll("conference space fail, already canceled",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("availableSpace", response.getAvailableSpace()),
                ()-> assertNull("rooCapacity",  response.getRoomCapacity()),
                ()-> assertNull("participantCount",  response.getParticipantsCount()),
                ()-> assertEquals("reason","Conference isn't available", response.getReason())
        );
    }

    @Test
    void conferenceUpdate() {
        roomCreate();
        conferenceCreate();

        createConferenceCreateRequest(response.getBookedFrom().toString(), response.getBookedUntil().toString(), null, conferenceValidationUUID);
        response = controller.conferenceUpdate(request);
        assertAll("conference update with same roomUUID success",
                ()-> assertNotNull("Response", response),
                ()-> assertNotEquals("validationUUID", conferenceValidationUUID, response.getValidationUUID()),
                ()-> assertEquals("validationUUID", conferenceValidationUUID, response.getOldValidationUUID()),
                ()-> assertEquals("conferennceUUID", conferenceUUID, response.getConferenceUUID()),
                ()-> assertEquals("bookedFrom", request.getFrom(), response.getBookedFrom()),
                ()-> assertEquals("bookedUntil", request.getUntil(), response.getBookedUntil())
        );
        Response responseCurrent = response;

        roomCreate();
        createConferenceCreateRequest(responseCurrent.getBookedFrom().toString(), responseCurrent.getBookedUntil().toString(), roomUUID, responseCurrent.getValidationUUID());
        response = controller.conferenceUpdate(request);
        System.out.println(request);
        System.out.println(response);
        assertAll("conference update with roomUUID success",
                ()-> assertNotNull("Response", response),
                ()-> assertNotEquals("validationUUID", conferenceValidationUUID, response.getValidationUUID()),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getOldValidationUUID()),
                ()-> assertNotEquals("conferenceUUID", conferenceUUID, response.getConferenceUUID()),
                ()-> assertEquals("bookedFrom", request.getFrom(), response.getBookedFrom()),
                ()-> assertEquals("bookedUntil", request.getUntil(), response.getBookedUntil())
        );
    }

    private void createConferenceUUIDRequest(UUID validationUUID) {
        UUID vUUID = validationUUID;
        if(vUUID == null) {
            vUUID = conferenceValidationUUID;
        }
        request = new  Request(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                vUUID,
                null
        );
    }

    private void createConferenceCreateRequest(String from, String until) {
        createConferenceCreateRequest(from, until, null, null);
    }

    private void createConferenceCreateRequest(String from, String until, UUID rUUID) {
        createConferenceCreateRequest(from, until, rUUID, null);
    }

    private void createConferenceCreateRequest(String from, String until, UUID rUUID, UUID validationUUID) {
        UUID room = rUUID;
        if(rUUID == null){
            room = roomUUID;
        }
        request = new  Request(
                null,
                null,
                null,
                null,
                room,
                "Some info as example",
                LocalDateTime.parse(from),
                LocalDateTime.parse(until),
                validationUUID,
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
        int roomCapacity = 100;
        String roomDescription = "For tet purpose";
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