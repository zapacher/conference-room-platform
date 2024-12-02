package ee.ctob.api.controller;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ee.ctob.access.ConferenceDAO;
import ee.ctob.access.ParticipantDAO;
import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.data.Conference;
import ee.ctob.data.Participant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;
import testutils.TestContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ee.ctob.data.enums.RoomStatus.AVAILABLE;
import static ee.ctob.data.enums.RoomStatus.CLOSED;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static testutils.ObjectCreators.*;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
public class MvcTestsBackoffice extends TestContainer {

    @MockBean
    ParticipantDAO participantDAO;

    @SpyBean
    ConferenceDAO conferenceDAO;

    @Autowired
    MockMvc mockMvc;

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
        performMvc("/backoffice/room/create");
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

        request = createRoomUpdateRequest(roomValidationUUID, null, CLOSED, 50);
        performMvc("/backoffice/room/update");
        assertAll("Room update fail, double update",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("roomUUID", response.getRoomUUID()),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertNull("status", response.getRoomStatus()),
                ()-> assertNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("reason", "Please provide new room status OR new capacity", response.getReason())
        );

        request = createRoomUpdateRequest(roomValidationUUID, UUID.randomUUID(), CLOSED, null);
        performMvc("/backoffice/room/update");
        assertAll("Room update fail, validation uuid not valid",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("roomUUID", response.getRoomUUID()),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertNull("status", response.getRoomStatus()),
                ()-> assertNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("reason", "Room not found, check validationUUID", response.getReason())
        );

        request = createRoomUpdateRequest(roomValidationUUID, null, AVAILABLE, null);
        performMvc("/backoffice/room/update");
        assertAll("Room update fail, status is the same",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("roomUUID", response.getRoomUUID()),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertNull("status", response.getRoomStatus()),
                ()-> assertNull("capacity", response.getRoomCapacity()),
                ()-> assertEquals("reason", "Room status is already : AVAILABLE", response.getReason())
        );
    }

    @Test
    void roomUpdateStatus() {
        roomCreate();

        request = createRoomUpdateRequest(roomValidationUUID, null, CLOSED, null);
        performMvc("/backoffice/room/update");
        assertAll("Room update success status",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("roomUUID", roomUUID, response.getRoomUUID()),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertEquals("capacity", roomCapacity, (int) response.getRoomCapacity()),
                ()-> assertEquals("status", request.getStatus(), response.getRoomStatus()),
                ()-> assertNull("reason", response.getReason())
        );

        request = createRoomUpdateRequest(roomValidationUUID, null, AVAILABLE, null);
        performMvc("/backoffice/room/update");
        assertAll("Room update success status",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("roomUUID", roomUUID, response.getRoomUUID()),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertEquals("capacity", roomCapacity, (int) response.getRoomCapacity()),
                ()-> assertEquals("status", AVAILABLE, response.getRoomStatus()),
                ()-> assertNull("reason", response.getReason())
        );

        request = createRoomUpdateRequest(roomValidationUUID, null, null, 40);
        performMvc("/backoffice/room/update");
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

        createConferenceCreateRequestOverlap("2024-12-28T10:00:00", "2024-12-28T15:00:00");
        performMvc("/backoffice/conference/create");
        assertAll("Conference create success",
                ()-> assertNotNull("Response", response),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNotNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertEquals("bookedFrom", request.getFrom(), response.getBookedFrom()),
                ()-> assertEquals("bookedUntil", request.getUntil(), response.getBookedUntil()),
                ()-> assertNull("reason", response.getReason())
        );

        createConferenceCreateRequestOverlap("2024-12-31T10:00:00", "2024-12-31T15:00:00");
        performMvc("/backoffice/conference/create");
        assertAll("Conference create success with other time",
                ()-> assertNotNull("Response", response),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNotNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertEquals("bookedFrom", request.getFrom(), response.getBookedFrom()),
                ()-> assertEquals("bookedUntil", request.getUntil(), response.getBookedUntil()),
                ()-> assertNull("reason", response.getReason())
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
        performMvc("/backoffice/conference/create");
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequestOverlap("2024-12-31T09:00:00", "2024-12-31T16:00:00");
        performMvc("/backoffice/conference/create");
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequestOverlap("2024-12-31T09:00:00", "2024-12-31T10:00:01");
        performMvc("/backoffice/conference/create");
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequestOverlap("2024-12-31T14:59:59", "2024-12-31T16:00:00");
        performMvc("/backoffice/conference/create");
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        createConferenceCreateRequestOverlap("2024-12-31T14:59:59", "2024-12-31T16:00:00");
        performMvc("/backoffice/conference/create");
        assertAll("Create conference fail overlapping time",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Chosen time isn't available", response.getReason())
        );

        request = createConferenceCreateRequest("2024-12-20T10:00:00", "2024-12-20T08:00:00", UUID.randomUUID(), conferenceValidationUUID);
        performMvc("/backoffice/conference/create");
        assertAll("Create conference fail roomUUID not valid",
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
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(conferenceValidationUUID);
        performMvc("/backoffice/conference/cancel");
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
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(UUID.randomUUID());
        performMvc("/backoffice/conference/cancel");
        assertAll("Conference cancel fail, uuid not valid",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Conference is already canceled or not exists", response.getReason())
        );

        conferenceCancel();
        request = createConferenceUUIDRequest(conferenceValidationUUID);
        performMvc("/backoffice/conference/cancel");
        assertAll("Conference cancel fail, already canceled",
                ()-> assertNotNull("Response", response),
                ()-> assertNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Conference is already canceled or not exists", response.getReason())
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
        performMvc("/backoffice/conference/feedbacks");
        assertAll("Conference feedback OK",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("participants count", roomCapacity, response.getFeedbackList().size()),
                ()-> assertNotNull("participant shortname", response.getFeedbackList().get(0).getShortName()),
                ()-> assertNotNull("participant feedback", response.getFeedbackList().get(0).getFeedback())
        );
    }

    @Test
    void conferenceFeedbackFail() {
        roomCreate();
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(conferenceValidationUUID);
        performMvc("/backoffice/conference/feedbacks");
        assertAll("conference feedbacks fail, no participants",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertNull("conferenceUUID", response.getConferenceUUID()),
                ()-> assertNull("bookedFrom", response.getBookedFrom()),
                ()-> assertNull("bookedUntil", response.getBookedUntil()),
                ()-> assertEquals("reason","Conference has no participants", response.getReason())
        );

        request = createConferenceUUIDRequest(UUID.randomUUID());
        performMvc("/backoffice/conference/feedbacks");
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
        withoutRoom = true;
        conferenceCreate();

        request = createConferenceUUIDRequest(conferenceValidationUUID);
        performMvc("/backoffice/conference/space");
        assertAll("Conference space success",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertEquals("availableSpace", (Integer) 100, response.getAvailableSpace()),
                ()-> assertEquals("rooCapacity", (Integer) 100, response.getRoomCapacity()),
                ()-> assertEquals("participantCount", (Integer) 0, response.getParticipantsCount()),
                ()-> assertNull("reason", response.getReason())
        );

        roomCreate();
        conferenceCreate();

        mockSpace(20);
        request = createConferenceUUIDRequest(conferenceValidationUUID);
        performMvc("/backoffice/conference/space");
        assertAll("Conference space success",
                ()-> assertNotNull("Response", response),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getValidationUUID()),
                ()-> assertEquals("availableSpace", (Integer) 80, response.getAvailableSpace()),
                ()-> assertEquals("rooCapacity", (Integer) 100, response.getRoomCapacity()),
                ()-> assertEquals("participantCount", (Integer) 20, response.getParticipantsCount()),
                ()-> assertNull("reason", response.getReason())
        );

    }

    @Test
    void conferenceSpaceFail() {
        roomCreate();
        conferenceCreate();

        request = createConferenceUUIDRequest(UUID.randomUUID());
        performMvc("/backoffice/conference/space");
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

        request = createConferenceCreateRequest(response.getBookedFrom().toString(), response.getBookedUntil().toString(), roomUUID, conferenceValidationUUID);
        performMvc("/backoffice/conference/update");
        assertAll("conference update with same roomUUID success",
                ()-> assertNotNull("Response", response),
                ()-> assertNotEquals("validationUUID", conferenceValidationUUID, response.getValidationUUID()),
                ()-> assertEquals("validationUUID", conferenceValidationUUID, response.getOldValidationUUID()),
                ()-> assertEquals("conferenceUUID", conferenceUUID, response.getConferenceUUID()),
                ()-> assertEquals("bookedFrom", request.getFrom(), response.getBookedFrom()),
                ()-> assertEquals("bookedUntil", request.getUntil(), response.getBookedUntil())
        );
        Response responseCurrent = response;

        roomCreate();

        request = createConferenceCreateRequest(responseCurrent.getBookedFrom().toString(), responseCurrent.getBookedUntil().toString(), roomUUID, responseCurrent.getValidationUUID());
        performMvc("/backoffice/conference/update");
        assertAll("conference update with roomUUID success",
                ()-> assertNotNull("Response", response),
                ()-> assertNotEquals("validationUUID", conferenceValidationUUID, response.getValidationUUID()),
                ()-> assertEquals("validationUUID", request.getValidationUUID(), response.getOldValidationUUID()),
                ()-> assertNotEquals("conferenceUUID", conferenceUUID, response.getConferenceUUID()),
                ()-> assertEquals("bookedFrom", request.getFrom(), response.getBookedFrom()),
                ()-> assertEquals("bookedUntil", request.getUntil(), response.getBookedUntil())
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

    private void mockSpace(int participantsCount) {
        Conference conference = Conference.builder()
                .validationUUID(conferenceValidationUUID)
                .conferenceUUID(conferenceUUID)
                .roomUUID(roomUUID)
                .participants(createParticipantUUIDList(participantsCount))
                .build();
        when(conferenceDAO.getConferenceByValidationUUID(conferenceValidationUUID)).thenReturn(conference);
    }

    private List<Participant> createParticipants(List<UUID> partcipantUUIDList){
        List<Participant> participantList = new ArrayList<>();
        for(int i = 0; partcipantUUIDList.size()>i; i++) {
            participantList.add(Participant.builder()
                    .firstName("Chuck"+i)
                    .lastName("Norris"+i)
                    .feedback("Test mocked feedback " +i)
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

    private void performMvc(String path){
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String responseMvc;
        System.out.println(request);
        try {
            responseMvc = mockMvc.perform(post(path)
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andReturn().getResponse().getContentAsString();
            System.out.println(responseMvc);
            response = mapper.readValue(responseMvc, Response.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}