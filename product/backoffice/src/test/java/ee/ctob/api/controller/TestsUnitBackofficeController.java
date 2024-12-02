package ee.ctob.api.controller;


import ee.ctob.access.ConferenceDAO;
import ee.ctob.access.ParticipantDAO;
import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.data.Conference;
import ee.ctob.data.Participant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import testutils.TestContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ee.ctob.data.enums.RoomStatus.AVAILABLE;
import static ee.ctob.data.enums.RoomStatus.CLOSED;
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

    @InjectMocks
    @Autowired
    BackofficeController controller;

    private UUID roomUUID;
    private UUID roomValidationUUID;
    private UUID conferenceUUID;
    private UUID conferenceValidationUUID;
    private Request request;
    private Response response;
    private boolean withoutRoom = false;
    private int roomCapacity = 100;

    @Test
    void roomCreate() {
        request = createRoomCreateRequest();
        response = controller.roomCreate(request);

        assertAll( "Room create success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNotNull(response.getRoomUUID(), "roomUUID"),
                ()-> assertNotNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getReason(), "reason")
        );

        roomUUID = response.getRoomUUID();
        roomValidationUUID = response.getValidationUUID();
    }

    @Test
    void roomUpdateFail() {
        roomCreate();

        request = createRoomUpdateRequest(roomValidationUUID, null, CLOSED, 50);
        response = controller.roomUpdate(request);
        assertAll("Room update fail, double update",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getRoomUUID(), "roomUUID"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(),"validationUUID"),
                ()-> assertNull(response.getRoomStatus(),"status"),
                ()-> assertNull(response.getRoomCapacity(), "capacity"),
                ()-> assertEquals("Please provide new room status OR new capacity", response.getReason(), "reason")
        );

        request = createRoomUpdateRequest(roomValidationUUID, UUID.randomUUID(), CLOSED, null);
        response = controller.roomUpdate(request);
        assertAll("Room update fail, validation uuid not valid",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getRoomUUID(), "roomUUID"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getRoomStatus(), "status"),
                ()-> assertNull(response.getRoomCapacity(), "capacity"),
                ()-> assertEquals("Room not found, check validationUUID", response.getReason(), "reason")
        );

        request = createRoomUpdateRequest(roomValidationUUID, null, AVAILABLE, null);
        response = controller.roomUpdate(request);
        assertAll("Room update fail, status is the same",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getRoomUUID(), "roomUUID"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getRoomStatus(), "status"),
                ()-> assertNull(response.getRoomCapacity(), "capacity"),
                ()-> assertEquals("Room status is already : AVAILABLE", response.getReason(), "reason")
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
                ()-> assertEquals(request.getStatus(), response.getRoomStatus(), "status"),
                ()-> assertNull(response.getReason(), "reason")
        );

        request = createRoomUpdateRequest(roomValidationUUID, null, AVAILABLE, null);
        response = controller.roomUpdate(request);
        assertAll("Room update success status",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(roomUUID, response.getRoomUUID(), "roomUUID"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(roomCapacity, response.getRoomCapacity(), "capacity"),
                ()-> assertEquals(request.getStatus(), response.getRoomStatus(), "status"),
                ()-> assertNull(response.getReason(), "reason")
        );

        int roomNewCapacity = 40;
        request = createRoomUpdateRequest(roomValidationUUID, null, null, roomNewCapacity);
        response= controller.roomUpdate(request);
        assertAll("Room update success capacity",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(roomUUID, response.getRoomUUID(), "roomUUID"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(roomNewCapacity, response.getRoomCapacity(), "capacity"),
                ()-> assertEquals(AVAILABLE, response.getRoomStatus(), "status"),
                ()-> assertNull(response.getReason(), "reason")
        );
    }

    @Test
    void conferenceCreate() {
        if(!withoutRoom) {
            roomCreate();
        }

        createConferenceCreateRequestOverlap("2024-12-31T10:00:00", "2024-12-31T15:00:00");
        response = controller.conferenceCreate(request);
        assertAll("Conference create success with other time",
                ()-> assertNotNull(response,"Response"),
                ()-> assertNotNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNotNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertEquals(request.getFrom(), response.getBookedFrom(), "bookedFrom"),
                ()-> assertEquals(request.getUntil(), response.getBookedUntil(), "bookedUntil"),
                ()-> assertNull(response.getReason(), "reason")
        );
        conferenceUUID = response.getConferenceUUID();
        conferenceValidationUUID = response.getValidationUUID();
    }

    @Test
    void conferenceCreateFail() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        createConferenceCreateRequestOverlap("2024-12-31T10:00:00", "2024-12-31T15:00:00");
        response = controller.conferenceCreate(request);
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertEquals("Chosen time isn't available", response.getReason(), "reason")
        );

        createConferenceCreateRequestOverlap("2024-12-31T09:00:00", "2024-12-31T16:00:00");
        response = controller.conferenceCreate(request);
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertEquals("Chosen time isn't available", response.getReason(), "reason")
        );

        createConferenceCreateRequestOverlap("2024-12-31T09:00:00", "2024-12-31T10:00:01");
        response = controller.conferenceCreate(request);
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertEquals("Chosen time isn't available", response.getReason(), "reason")
        );

        createConferenceCreateRequestOverlap("2024-12-31T14:59:59", "2024-12-31T16:00:00");
        response = controller.conferenceCreate(request);
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertEquals("Chosen time isn't available", response.getReason(), "reason")
        );

        createConferenceCreateRequestOverlap("2024-12-31T14:59:59", "2024-12-31T16:00:00");
        response = controller.conferenceCreate(request);
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertEquals("Chosen time isn't available", response.getReason(), "reason")
        );

        roomCreate();
        request = createRoomUpdateRequest(roomValidationUUID, null, CLOSED, null);
        response = controller.roomUpdate(request);

        createConferenceCreateRequest("2024-12-20T10:00:00", "2024-12-20T08:00:00", roomUUID, conferenceValidationUUID);
        response = controller.conferenceCreate(request);
        assertAll("Create conference fail roomUUID not valid",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertEquals("Chosen room isn't available", response.getReason(), "reason")
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
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertNull(response.getReason(), "reason")
        );
    }

    @Test
    void conferenceCancelFail() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(UUID.randomUUID());
        response = controller.conferenceCancel(request);
        assertAll("Conference cancel fail, uuid not valid",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(),"validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertEquals("Conference is already canceled or not exists", response.getReason(), "reason")
        );

        conferenceCancel();
        request = createConferenceUUIDRequest(conferenceValidationUUID);
        response = controller.conferenceCancel(request);
        assertAll("Conference cancel fail, already canceled",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(),"validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertEquals("Conference is already canceled or not exists", response.getReason(), "reason")
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
                ()-> assertNotNull(response.getFeedbackList().get(0).getFeedback(), "participant feedback"),
                ()-> assertNull(response.getReason(), "reason")
        );
    }

    @Test
    void conferenceFeedbackFail() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(conferenceValidationUUID);
        response = controller.conferenceFeedbacks(request);
        assertAll("conference feedbacks fail, no participants",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertEquals("Conference has no participants", response.getReason(), "reason")
        );

        mockEmptyFeedbacks(20);
        response = controller.conferenceFeedbacks(request);
        assertAll("conference feedback fail, no feedbacks",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertEquals("No feedback for this conference", response.getReason(), "reason")
        );

        request = createConferenceUUIDRequest(UUID.randomUUID());
        response = controller.conferenceFeedbacks(request);
        assertAll("conference feedback fail, already canceled",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertNull(response.getBookedFrom(), "bookedFrom"),
                ()-> assertNull(response.getBookedUntil(), "bookedUntil"),
                ()-> assertEquals("Conference doesn't exists", response.getReason(), "reason")
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
                ()-> assertEquals((Integer) 0, response.getParticipantsCount(), "participantCount"),
                ()-> assertNull(response.getReason(), "reason")
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
                ()-> assertEquals(participantCount, response.getParticipantsCount(), "participantCount"),
                ()-> assertNull(response.getReason(), "reason")
        );
    }

    @Test
    void conferenceSpaceFail() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(UUID.randomUUID());
        response = controller.conferenceSpace(request);
        assertAll("conference space fail, already canceled",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getAvailableSpace(), "availableSpace"),
                ()-> assertNull(response.getRoomCapacity(), "roomCapacity"),
                ()-> assertNull(response.getParticipantsCount(), "participantCount"),
                ()-> assertEquals("Conference isn't available", response.getReason(), "reason")
        );
    }

    @Test
    void conferenceUpdate() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceCreateRequest(response.getBookedFrom().toString(), response.getBookedUntil().toString(), roomUUID, conferenceValidationUUID);
        response = controller.conferenceUpdate(request);
        assertAll("conference update with same roomUUID success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNotEquals(conferenceValidationUUID, response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(conferenceValidationUUID, response.getOldValidationUUID(), "validationUUID"),
                ()-> assertEquals(conferenceUUID, response.getConferenceUUID(), "conferennceUUID"),
                ()-> assertEquals(request.getFrom(), response.getBookedFrom(), "bookedFrom"),
                ()-> assertEquals(request.getUntil(), response.getBookedUntil(), "bookedUntil")
        );
        Response responseCurrent = response;

        roomCreate();

        request = createConferenceCreateRequest(responseCurrent.getBookedFrom().toString(), responseCurrent.getBookedUntil().toString(), roomUUID, responseCurrent.getValidationUUID());
        response = controller.conferenceUpdate(request);
        assertAll("conference update with roomUUID success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNotEquals(conferenceValidationUUID, response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(request.getValidationUUID(), response.getOldValidationUUID(), "validationUUID"),
                ()-> assertNotEquals(conferenceUUID, response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertEquals(request.getFrom(), response.getBookedFrom(), "bookedFrom"),
                ()-> assertEquals(request.getUntil(), response.getBookedUntil(), "bookedUntil")
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

        when(conferenceDAO.getConferenceByValidationUUID(conferenceValidationUUID)).thenReturn(conference);
        when(participantDAO.findByParticipantUUIDs(partipicantUUIDList)).thenReturn(createParticipants(partipicantUUIDList));
    }

    private void mockEmptyFeedbacks(int participantCount) {
        List<UUID> partipicantUUIDList = createParticipantUUIDList(participantCount);
        Conference conference = Conference.builder()
                .validationUUID(conferenceValidationUUID)
                .conferenceUUID(conferenceUUID)
                .roomUUID(roomUUID)
                .participants(partipicantUUIDList)
                .build();

        List<Participant> participantList = new ArrayList<>();
        when(conferenceDAO.getConferenceByValidationUUID(conferenceValidationUUID)).thenReturn(conference);
        when(participantDAO.findByParticipantUUIDs(partipicantUUIDList)).thenReturn(participantList);
    }

    private void mockSpace(int participantsCount) {
        Conference conference = Conference.builder()
                .validationUUID(conferenceValidationUUID)
                .conferenceUUID(conferenceUUID)
                .roomUUID(roomUUID)
                .participants(createParticipantUUIDList(participantsCount))
                .build();
        when(conferenceDAO.getConferenceByValidationUUID(conferenceValidationUUID)).thenReturn(conference);
    }

    private List<Participant> createParticipants(List<UUID> partcipantUUIDList) {
        List<Participant> participantList = new ArrayList<>();
        for(int i = 0; partcipantUUIDList.size()>i; i++) {
            participantList.add(
                    Participant.builder()
                            .firstName("Chuck" + i)
                            .lastName("Norris" + i)
                            .feedback("Test mocked feedback " + i)
                            .build());
        }
        return participantList;
    }

    private List<UUID> createParticipantUUIDList(int participantsCount) {
        List<UUID> participantList = new ArrayList<>();
        for(int i = 0; i<participantsCount;i++ ) {
            participantList.add(UUID.randomUUID());
        }
        return participantList;
    }

    private void createConferenceCreateRequestOverlap(String from, String until) {
        request = createConferenceCreateRequest(from, until, roomUUID, conferenceValidationUUID);
    }
}