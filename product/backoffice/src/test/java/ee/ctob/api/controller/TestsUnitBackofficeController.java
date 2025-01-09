package ee.ctob.api.controller;


import ee.ctob.access.ConferenceDAO;
import ee.ctob.access.ParticipantDAO;
import ee.ctob.access.RoomDAO;
import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.error.BadRequestException;
import ee.ctob.api.error.ErrorResponse;
import ee.ctob.api.error.PreconditionsFailedException;
import ee.ctob.data.Conference;
import ee.ctob.data.Participant;
import ee.ctob.data.Room;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import testutils.TestContainer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ee.ctob.data.enums.RoomStatus.AVAILABLE;
import static ee.ctob.data.enums.RoomStatus.CLOSED;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static testutils.ObjectCreators.*;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class TestsUnitBackofficeController extends TestContainer {

    @MockBean
    ParticipantDAO participantDAO;

    @SpyBean
    ConferenceDAO conferenceDAO;

    @Autowired
    RoomDAO roomDAO;

    @InjectMocks
    @Autowired
    BackofficeController controller;

    private UUID roomUUID;
    private UUID roomValidationUUID;
    private UUID conferenceUUID;
    private UUID conferenceValidationUUID;
    private Request request;
    private Response response;
    private ErrorResponse errorResponse;
    private boolean withoutRoom = false;
    private int roomCapacity = 100;

    @Test
    void roomCreate() {
        request = createRoomCreateRequest();
        response = controller.roomCreate(request);

        assertAll( "Room create success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNotNull(response.getRoomUUID(), "roomUUID"),
                ()-> assertNotNull(response.getValidationUUID(), "validationUUID")
        );

        roomUUID = response.getRoomUUID();
        roomValidationUUID = response.getValidationUUID();
    }

    @Test
    void roomUpdateFail() {
        roomCreate();

        request = createRoomUpdateRequest(roomValidationUUID, null, CLOSED, 50);
        errorResponse = assertThrows(BadRequestException.class,
                ()-> controller.roomUpdate(request)).getError();
        assertAll("Room update fail, double update",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(400, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Please provide new room status OR new capacity", errorResponse.getMessage(), "error message")
        );

        request = createRoomUpdateRequest(roomValidationUUID, UUID.randomUUID(), CLOSED, null);
        errorResponse = assertThrows(BadRequestException.class,
                ()-> controller.roomUpdate(request)).getError();
        assertAll("Room update fail, validation uuid not valid",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(400, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Room not found, check validationUUID", errorResponse.getMessage(), "error message")
        );

        request = createRoomUpdateRequest(roomValidationUUID, null, AVAILABLE, null);
        errorResponse = assertThrows(BadRequestException.class,
                ()-> controller.roomUpdate(request)).getError();
        assertAll("Room update fail, status is the same",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(400, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Room status is already : " + request.getStatus(), errorResponse.getMessage(), "error message")
        );
    }

    @Test
    void roomUpdateStatus() {
        roomCreate();

        request = createRoomUpdateRequest(roomValidationUUID, null, CLOSED, null);
        response = controller.roomUpdate(request);
        assertAll("Room update success status",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(roomUUID, response.getRoomUUID(), "roomUUID"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(roomCapacity, response.getRoomCapacity(), "capacity"),
                ()-> assertEquals(request.getStatus(), response.getRoomStatus(), "status")
        );

        request = createRoomUpdateRequest(roomValidationUUID, null, AVAILABLE, null);
        response = controller.roomUpdate(request);
        assertAll("Room update success status",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(roomUUID, response.getRoomUUID(), "roomUUID"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(roomCapacity, response.getRoomCapacity(), "capacity"),
                ()-> assertEquals(request.getStatus(), response.getRoomStatus(), "status")
        );

        int roomNewCapacity = 40;
        request = createRoomUpdateRequest(roomValidationUUID, null, null, roomNewCapacity);
        response = controller.roomUpdate(request);
        Room room = roomDAO.getRoomByValidationUUID(roomValidationUUID).get();
        assertAll("Room update success capacity",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(roomUUID, response.getRoomUUID(), "roomUUID"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(roomNewCapacity, room.getCapacity(), "capacity"),
                ()-> assertEquals(AVAILABLE, response.getRoomStatus(), "status")
        );
    }

    @Test
    void conferenceCreate() {
        if(!withoutRoom) {
            roomCreate();
        }

        createConferenceCreateRequestOverlap(now().plusHours(60), now().plusHours(65));
        response = controller.conferenceCreate(request);
        assertAll("Conference create success with other time",
                ()-> assertNotNull(response,"Response"),
                ()-> assertNotNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNotNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertEquals(format(request.getFrom()), format(response.getBookedFrom()), "bookedFrom"),
                ()-> assertEquals(format(request.getUntil()), format(response.getBookedUntil()), "bookedUntil")
        );
        conferenceUUID = response.getConferenceUUID();
        conferenceValidationUUID = response.getValidationUUID();
    }

    @Test
    void conferenceCreateFail() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        createConferenceCreateRequestOverlap(now().plusHours(60), now().plusHours(65));
        errorResponse = assertThrows(PreconditionsFailedException.class,
                ()-> controller.conferenceCreate(request)).getError();
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Chosen time isn't available", errorResponse.getMessage(), "error message")
        );

        createConferenceCreateRequestOverlap(now().minusHours(60), now().plusHours(65));
        errorResponse = assertThrows(BadRequestException.class,
                ()-> controller.conferenceCreate(request)).getError();
        assertAll("Create conference bad time ",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(400, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Unlogical booking time", errorResponse.getMessage(), "error message")
        );

        createConferenceCreateRequestOverlap(now().plusHours(60), now().minusHours(65));
        errorResponse = assertThrows(BadRequestException.class,
                ()-> controller.conferenceCreate(request)).getError();
        assertAll("Create conference bad time ",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(400, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Unlogical booking time", errorResponse.getMessage(), "error message")
        );

        createConferenceCreateRequestOverlap(now().plusHours(59), now().plusHours(61));
        errorResponse = assertThrows(PreconditionsFailedException.class,
                ()-> controller.conferenceCreate(request)).getError();
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Chosen time isn't available", errorResponse.getMessage(), "error message")
        );

        createConferenceCreateRequestOverlap(now().plusHours(64), now().plusHours(66));
        errorResponse = assertThrows(PreconditionsFailedException.class,
                ()-> controller.conferenceCreate(request)).getError();
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Chosen time isn't available", errorResponse.getMessage(), "error message")
        );

        createConferenceCreateRequestOverlap(now().plusHours(59), now().plusHours(66));
        errorResponse= assertThrows(PreconditionsFailedException.class,
                ()-> controller.conferenceCreate(request)).getError();
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Chosen time isn't available", errorResponse.getMessage(), "error message")
        );

        roomCreate();
        request = createRoomUpdateRequest(roomValidationUUID, null, CLOSED, null);
        response = controller.roomUpdate(request);

        request = createConferenceRequest(now().plusHours(60), now().plusHours(65), conferenceValidationUUID, roomUUID);
        errorResponse = assertThrows(PreconditionsFailedException.class,
                ()-> controller.conferenceCreate(request)).getError();
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Chosen room isn't available", errorResponse.getMessage(), "error message")
        );
    }

    @Test
    void conferenceCancel() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(conferenceValidationUUID);
        response = controller.conferenceCancel(request);
        assertAll("Conference cancel success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil")
        );
    }

    @Test
    void conferenceCancelFail() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(UUID.randomUUID());
        errorResponse = assertThrows(PreconditionsFailedException.class,
                ()-> controller.conferenceCancel(request)).getError();
        assertAll("Conference cancel fail, uuid not valid",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Conference is already canceled or not exists", errorResponse.getMessage(), "error message")
        );

        conferenceCancel();
        request = createConferenceUUIDRequest(conferenceValidationUUID);
        errorResponse = assertThrows(PreconditionsFailedException.class,
                ()-> controller.conferenceCancel(request)).getError();
        assertAll("Conference cancel fail, uuid not valid",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Conference is already canceled or not exists", errorResponse.getMessage(), "error message")
        );
    }

    @Test
    void setConferenceFeedbacks() {
        roomCapacity = 20;
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        mockFeedbacks(roomCapacity);

        request = createConferenceUUIDRequest(conferenceValidationUUID);
        response = controller.conferenceFeedbacks(request);
        System.out.println(response);
        assertAll("Conference feedback OK",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(roomCapacity, response.getFeedbackList().size(), "participants count"),
                ()-> assertNotNull(response.getFeedbackList().get(0).getShortName(), "participant shortname"),
                ()-> assertNotNull(response.getFeedbackList().get(0).getFeedback(), "participant feedback")
        );
    }

    @Test
    void conferenceFeedbackFail() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(conferenceValidationUUID);
        errorResponse = assertThrows(PreconditionsFailedException.class,
                ()-> controller.conferenceFeedbacks(request)).getError();
        assertAll("Conference feedbacks fail, no participants",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Conference has no participants", errorResponse.getMessage(), "error message")
        );

        mockEmptyFeedbacks();
        errorResponse = assertThrows(PreconditionsFailedException.class,
                ()-> controller.conferenceFeedbacks(request)).getError();
        assertAll("Conference feedbacks fail, no feedbacks",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("No feedback for this conference", errorResponse.getMessage(), "error message")
        );

        request = createConferenceUUIDRequest(UUID.randomUUID());
        errorResponse = assertThrows(PreconditionsFailedException.class,
                ()-> controller.conferenceFeedbacks(request)).getError();
        assertAll("Conference feedbacks fail, conference canceled",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Conference doesn't exists", errorResponse.getMessage(), "error message")
        );
    }

    @Test
    void conferenceSpace() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(conferenceValidationUUID);
        response = controller.conferenceSpace(request);
        assertAll("Conference space success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(roomCapacity, response.getAvailableSpace(), "availableSpace"),
                ()-> assertEquals(roomCapacity, response.getRoomCapacity(), "roomCapacity"),
                ()-> assertEquals((Integer) 0, response.getParticipantsCount(), "participantCount")
        );

        withoutRoom = false;
        conferenceCreate();

        int participantCount = 20;
        mockSpace(participantCount);
        request = createConferenceUUIDRequest(conferenceValidationUUID);
        response = controller.conferenceSpace(request);
        assertAll("Conference space success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(roomCapacity-participantCount, response.getAvailableSpace(), "availableSpace"),
                ()-> assertEquals(roomCapacity, response.getRoomCapacity(), "roomCapacity"),
                ()-> assertEquals(participantCount, response.getParticipantsCount(), "participantCount")
        );
    }

    @Test
    void conferenceSpaceFail() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(UUID.randomUUID());
        errorResponse = assertThrows(PreconditionsFailedException.class,
                ()-> controller.conferenceSpace(request)).getError();
        assertAll("Conference space fail, already canceled",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Conference isn't available", errorResponse.getMessage(), "error message")
        );
    }

    @Test
    void conferenceUpdate() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceRequest(response.getBookedFrom(), response.getBookedUntil(), conferenceValidationUUID, roomUUID);
        response = controller.conferenceUpdate(request);
        assertAll("conference update with same roomUUID success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNotEquals(conferenceValidationUUID, response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(conferenceUUID, response.getConferenceUUID(), "conferennceUUID"),
                ()-> assertEquals(format(request.getFrom()), format(response.getBookedFrom()), "bookedFrom"),
                ()-> assertEquals(format(request.getUntil()), format(response.getBookedUntil()), "bookedUntil")
        );
        Response responseCurrent = response;

        roomCreate();

        request = createConferenceRequest(responseCurrent.getBookedFrom(), responseCurrent.getBookedUntil(), responseCurrent.getValidationUUID(), roomUUID);
        response = controller.conferenceUpdate(request);
        assertAll("conference update with roomUUID success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNotEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertNotEquals(conferenceUUID, response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertEquals(format(request.getFrom()), format(response.getBookedFrom()), "bookedFrom"),
                ()-> assertEquals(format(request.getUntil()), format(response.getBookedUntil()), "bookedUntil")
        );
    }

    private void mockFeedbacks(int participantsCount) {
        List<UUID> partipicantUUIDList = createParticipantUUIDList(participantsCount);
        Conference conference = Conference.builder()
                .validationUUID(conferenceValidationUUID)
                .conferenceUUID(conferenceUUID)
                .roomUUID(roomUUID)
                .participants(partipicantUUIDList)
                .build();

        when(conferenceDAO.getConferenceByValidationUUID(conferenceValidationUUID)).thenReturn(Optional.of(conference));
        when(participantDAO.findByParticipantUUIDs(partipicantUUIDList)).thenReturn(createParticipants(partipicantUUIDList));
    }

    private void mockEmptyFeedbacks() {
        List<UUID> partipicantUUIDList = createParticipantUUIDList(10);
        Conference conference = Conference.builder()
                .validationUUID(conferenceValidationUUID)
                .conferenceUUID(conferenceUUID)
                .roomUUID(roomUUID)
                .participants(partipicantUUIDList)
                .build();

        List<Participant> participantList = new ArrayList<>();
        when(conferenceDAO.getConferenceByValidationUUID(conferenceValidationUUID)).thenReturn(Optional.of(conference));
        when(participantDAO.findByParticipantUUIDs(partipicantUUIDList)).thenReturn(Optional.of(participantList));
    }

    private void mockSpace(int participantsCount) {
        Conference conference = Conference.builder()
                .validationUUID(conferenceValidationUUID)
                .conferenceUUID(conferenceUUID)
                .roomUUID(roomUUID)
                .participants(createParticipantUUIDList(participantsCount))
                .build();
        when(conferenceDAO.getConferenceByValidationUUID(conferenceValidationUUID)).thenReturn(Optional.of(conference));
    }

    private Optional<List<Participant>> createParticipants(List<UUID> partcipantUUIDList) {
        List<Participant> participantList = new ArrayList<>();
        for(int i = 0; partcipantUUIDList.size()>i; i++) {
            participantList.add(
                    Participant.builder()
                            .firstName("Chuck" + i)
                            .lastName("Norris" + i)
                            .feedback("Test mocked feedback " + i)
                            .build());
        }
        return Optional.of(participantList);
    }

    private List<UUID> createParticipantUUIDList(int participantsCount) {
        List<UUID> participantList = new ArrayList<>();
        for(int i = 0; i<participantsCount;i++ ) {
            participantList.add(UUID.randomUUID());
        }
        return participantList;
    }

    private void createConferenceCreateRequestOverlap(LocalDateTime from, LocalDateTime until) {
        request = createConferenceRequest(from, until, conferenceValidationUUID, roomUUID);
    }
}