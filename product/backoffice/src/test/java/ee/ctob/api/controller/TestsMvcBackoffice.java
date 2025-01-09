package ee.ctob.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ee.ctob.access.ConferenceDAO;
import ee.ctob.access.ParticipantDAO;
import ee.ctob.access.RoomDAO;
import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.error.ErrorResponse;
import ee.ctob.data.Conference;
import ee.ctob.data.Participant;
import ee.ctob.data.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static testutils.ObjectCreators.*;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
public class TestsMvcBackoffice extends TestContainer {

    @Autowired
    RoomDAO roomDAO;
    @Autowired
    MockMvc mockMvc;

    @MockBean
    ParticipantDAO participantDAO;

    @SpyBean
    ConferenceDAO conferenceDAO;

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
    void emptyRequest400() {
        request = new Request(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        performMvcThrow("/backoffice/room/create");
        assertNull(errorResponse, "errorResponse");

        performMvcThrow("/backoffice/room/update");
        assertNull(errorResponse, "errorResponse");

        performMvcThrow("/backoffice/conference/create");
        assertNull(errorResponse, "errorResponse");

        performMvcThrow("/backoffice/conference/update");
        assertNull(errorResponse, "errorResponse");

        performMvcThrow("/backoffice/conference/space");
        assertNull(errorResponse, "errorResponse");

        performMvcThrow("/backoffice/conference/feedback");
        assertNull(errorResponse, "errorResponse");

        performMvcThrow("/backoffice/conference/cancel");
        assertNull(errorResponse, "errorResponse");
    }

    @Test
    void roomCreate() {
        request = createRoomCreateRequest();

        performMvc("/backoffice/room/create");
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
        performMvcThrow("/backoffice/room/update");
        assertAll("Room update fail, double update",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(400, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Please provide new room status OR new capacity", errorResponse.getMessage(), "error message")
        );

        request = createRoomUpdateRequest(roomValidationUUID, UUID.randomUUID(), CLOSED, null);
        performMvcThrow("/backoffice/room/update");
        assertAll("Room update fail, validation uuid not valid",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(400, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Room not found, check validationUUID", errorResponse.getMessage(), "error message")
        );

        request = createRoomUpdateRequest(roomValidationUUID, null, AVAILABLE, null);
        performMvcThrow("/backoffice/room/update");
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
        performMvc("/backoffice/room/update");
        assertAll("Room update success status",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(roomUUID, response.getRoomUUID(), "roomUUID"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(roomCapacity, response.getRoomCapacity(), "capacity"),
                ()-> assertEquals(request.getStatus(), response.getRoomStatus(), "status")
        );

        request = createRoomUpdateRequest(roomValidationUUID, null, AVAILABLE, null);
        performMvc("/backoffice/room/update");
        assertAll("Room update success status",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(roomUUID, response.getRoomUUID(), "roomUUID"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(roomCapacity, response.getRoomCapacity(), "capacity"),
                ()-> assertEquals(request.getStatus(), response.getRoomStatus(), "status")
        );

        int roomNewCapacity = 40;
        request = createRoomUpdateRequest(roomValidationUUID, null, null, roomNewCapacity);
        performMvc("/backoffice/room/update");
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

        createConferenceCreateRequestOnlyTime(now().plusHours(60), now().plusHours(65));
        performMvc("/backoffice/conference/create");
        assertAll("Conference create success with other time",
                ()-> assertNotNull(response,"Response"),
                ()-> assertNotNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNotNull(response.getConferenceUUID(), "conferenceUUID"),
                ()-> assertEquals(format(request.getFrom()), response.getBookedFrom(), "bookedFrom"),
                ()-> assertEquals(format(request.getUntil()), response.getBookedUntil(), "bookedUntil")
        );
        conferenceUUID = response.getConferenceUUID();
        conferenceValidationUUID = response.getValidationUUID();
    }

    @Test
    void conferenceCreateFail() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        createConferenceCreateRequestOnlyTime(now().plusHours(60), now().plusHours(65));
        performMvcThrow("/backoffice/conference/create");
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Chosen time isn't available", errorResponse.getMessage(), "error message")
        );

        createConferenceCreateRequestOnlyTime(now().minusHours(60), now().plusHours(65));
        performMvcThrow("/backoffice/conference/create");
        assertAll("Create conference bad time ",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(400, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Unlogical booking time", errorResponse.getMessage(), "error message")
        );

        createConferenceCreateRequestOnlyTime(now().plusHours(60), now().minusHours(65));
        performMvcThrow("/backoffice/conference/create");
        assertAll("Create conference bad time ",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(400, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Unlogical booking time", errorResponse.getMessage(), "error message")
        );

        createConferenceCreateRequestOnlyTime(now().plusHours(59), now().plusHours(61));
        performMvcThrow("/backoffice/conference/create");
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Chosen time isn't available", errorResponse.getMessage(), "error message")
        );

        createConferenceCreateRequestOnlyTime(now().plusHours(64), now().plusHours(66));
        performMvcThrow("/backoffice/conference/create");
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Chosen time isn't available", errorResponse.getMessage(), "error message")
        );

        createConferenceCreateRequestOnlyTime(now().plusHours(59), now().plusHours(66));
        performMvcThrow("/backoffice/conference/create");
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Chosen time isn't available", errorResponse.getMessage(), "error message")
        );

        roomCreate();
        request = createRoomUpdateRequest(roomValidationUUID, null, CLOSED, null);
        performMvcThrow("/backoffice/room/update");

        request = createConferenceRequest(now().plusHours(60), now().plusHours(65), conferenceValidationUUID, roomUUID);
        performMvcThrow("/backoffice/conference/create");
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
        performMvc("/backoffice/conference/cancel");
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
        performMvcThrow("/backoffice/conference/cancel");
        assertAll("Conference cancel fail, uuid not valid",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Conference is already canceled or not exists", errorResponse.getMessage(), "error message")
        );

        conferenceCancel();
        request = createConferenceUUIDRequest(conferenceValidationUUID);
        performMvcThrow("/backoffice/conference/cancel");
        assertAll("Conference cancel fail, uuid not valid",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Conference is already canceled or not exists", errorResponse.getMessage(), "error message")
        );
    }

    @Test
    void conferenceFeedback() {
        roomCapacity = 20;
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        mockFeedbacks(roomCapacity);

        request = createConferenceUUIDRequest(conferenceValidationUUID);
        performMvc("/backoffice/conference/feedback");
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
        performMvcThrow("/backoffice/conference/feedback");
        assertAll("Conference feedbacks fail, no participants",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Conference has no participants", errorResponse.getMessage(), "error message")
        );

        mockEmptyFeedbacks();
        performMvcThrow("/backoffice/conference/feedback");
        assertAll("Conference feedbacks fail, no feedbacks",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("No feedback for this conference", errorResponse.getMessage(), "error message")
        );

        request = createConferenceUUIDRequest(UUID.randomUUID());
        performMvcThrow("/backoffice/conference/feedback");
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
        performMvc("/backoffice/conference/space");
        assertAll("Conference space success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(roomCapacity, response.getAvailableSpace(), "availableSpace"),
                ()-> assertEquals(roomCapacity, response.getRoomCapacity(), "roomCapacity"),
                ()-> assertEquals((Integer) 0, response.getParticipantsCount(), "participantCount")
        );

        roomCreate();
        conferenceCreate();

        int participantCount = 20;
        mockSpace(participantCount);
        request = createConferenceUUIDRequest(conferenceValidationUUID);
        performMvc("/backoffice/conference/space");
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
        conferenceCreate();

        request = createConferenceUUIDRequest(UUID.randomUUID());
        performMvcThrow("/backoffice/conference/space");
        assertAll("Conference space fail, already canceled",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Conference isn't available", errorResponse.getMessage(), "error message")
        );
    }

    @Test
    void conferenceUpdate() {
        roomCreate();
        conferenceCreate();

        request = createConferenceRequest(response.getBookedFrom(), response.getBookedUntil(), conferenceValidationUUID, roomUUID);
        performMvc("/backoffice/conference/update");
        assertAll("conference update with same roomUUID success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNotEquals(conferenceValidationUUID, response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals(conferenceUUID, response.getConferenceUUID(), "conferennceUUID"),
                ()-> assertEquals(request.getFrom(), response.getBookedFrom(), "bookedFrom"),
                ()-> assertEquals(request.getUntil(), response.getBookedUntil(), "bookedUntil")
        );
        Response responseCurrent = response;

        roomCreate();

        request = createConferenceRequest(responseCurrent.getBookedFrom(), responseCurrent.getBookedUntil(), responseCurrent.getValidationUUID(), roomUUID);
        performMvc("/backoffice/conference/update");
        assertAll("conference update with roomUUID success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNotEquals(request.getValidationUUID(), response.getValidationUUID(), "validationUUID"),
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

    private Optional<List<Participant>> createParticipants(List<UUID> partcipantUUIDList){
        List<Participant> participantList = new ArrayList<>();
        for(int i = 0; partcipantUUIDList.size()>i; i++) {
            participantList.add(Participant.builder()
                    .firstName("Chuck"+i)
                    .lastName("Norris"+i)
                    .feedback("Test mocked feedback " +i)
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

    private void createConferenceCreateRequestOnlyTime(LocalDateTime from, LocalDateTime until) {
        request = createConferenceRequest(from, until, conferenceValidationUUID, roomUUID);
    }

    private void performMvc(String path) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String responseMvc;
        try {
            responseMvc = mockMvc.perform(post(path)
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andReturn().getResponse().getContentAsString();
            response = mapper.readValue(responseMvc, Response.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void performMvcThrow(String path) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String responseMvc;
        try {
            responseMvc = mockMvc.perform(post(path)
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andReturn().getResponse().getContentAsString();
            errorResponse = mapper.readValue(responseMvc, ErrorResponse.class);
        } catch (Exception ignore) {
        }
    }
}