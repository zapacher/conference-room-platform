package ee.ctob.controller;

import ee.ctob.access.ConferenceDAO;
import ee.ctob.access.ParticipantDAO;
import ee.ctob.access.RoomDAO;
import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.controller.ConferenceController;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static testutils.ObjectCreators.*;

@Testcontainers
@ExtendWith(MockitoExtension.class)
public class TestsUnitConferenceController extends TestContainer {
    @MockBean
    RoomDAO roomDAO;
    @MockBean
    ConferenceDAO conferenceDAO;
    @SpyBean
    ParticipantDAO participantDAO;
    @InjectMocks
    @Autowired
    ConferenceController controller;

    Request request;
    Response response;
    UUID participantValidationUUID;

    @Test
    void registration() {
        request = createRegitrationRequest(UUID.randomUUID());
        mockRegistration();
        response = controller.registration(request);

        assertAll("Registration Success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNotNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getReason(), "reason")
        );

        participantValidationUUID = response.getValidationUUID();
    }

    @Test
    void registrationFail() {
        request = createRegitrationRequest(UUID.randomUUID());
        response = controller.registration(request);

        assertAll("Registration Fail",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals("Registration isn't available for this conference", response.getReason(), "response")
        );
    }

    @Test
    void registrationCancel() {
        registration();

        mockConferenceForCancel(UUID.randomUUID());
        mockParticipantForCancel(1);

        request = createRegitrationCancelRequest(participantValidationUUID);
        response = controller.registrationCancel(request);

        assertAll("Registration cancel success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getReason(), "response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertTrue(response.isRegistrationCancel(), "registrationCancel")
        );
    }

    @Test
    void registrationCancelFail() {
        registration();

        request = createRegitrationCancelRequest(UUID.randomUUID());
        response = controller.registrationCancel(request);

        assertAll("Registration Fail",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals("Participant with this validation doesn't exists", response.getReason(), "response"),
                ()-> assertFalse(response.isRegistrationCancel(), "registrationCancel")
        );

        request = createRegitrationCancelRequest(participantValidationUUID);
        mockConferenceForCancel(null);
        response = controller.registrationCancel(request);

        assertAll("Registration Fail",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals("Conference already started or finished", response.getReason(), "response"),
                ()-> assertFalse(response.isRegistrationCancel(), "registrationCancel")
        );

        request = createRegitrationCancelRequest(participantValidationUUID);
        mockConferenceForCancel(UUID.randomUUID());
        mockParticipantForCancel(0);
        response = controller.registrationCancel(request);


        assertAll("Registration Fail",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals("Validation uuid isnt valid", response.getReason(), "response"),
                ()-> assertFalse(response.isRegistrationCancel(), "registrationCancel")
        );
    }

    @Test
    void feedback() {
        registration();

        request = createFeedbackRequest(participantValidationUUID, "Any text for feedback");
        mockFeedback(1);
        response = controller.feedback(request);

        assertAll("Feedback Success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(participantValidationUUID, response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getReason(), "response"),
                ()-> assertTrue(response.isFeedbackResult(), "feedbackResult")
        );
    }

    @Test
    void feedbackFail() {
        registration();

        request = createFeedbackRequest(participantValidationUUID, "Any text for feedback");
        mockFeedback(0);
        response = controller.feedback(request);

        assertAll("Feedback Fail",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(participantValidationUUID, response.getValidationUUID(), "validationUUID"),
                ()-> assertEquals("Feedback already exists or conference isn't finished", response.getReason(), "response"),
                ()-> assertFalse(response.isFeedbackResult(), "feedbackResult")
        );
    }

    @Test
    void availableConferences() {
        registration();

        mockConferenceList(true);
        mockLocation();

        request = createRequestForConfernces("2024-12-10T15:00:00", "2024-12-31T15:00:00");
        response = controller.availableConferences(request);

        assertAll("Available conferences Fail",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(5, response.getConferenceAvailableList().size(), "conferenceAvailableList"),
                ()-> assertNull(response.getReason(), "reason")
        );

        for(Response.ConferenceAvailable conference : response.getConferenceAvailableList()) {
            assertAll("Conference from availableList",
                    ()-> assertNotNull(conference.getConferenceUUID(), "conferenceUUID"),
                    ()-> assertNotNull(conference.getParticipantsAmount(), "participantsAmount"),
                    ()-> assertNotNull(conference.getFrom(), "from"),
                    ()-> assertNotNull(conference.getUntil(), "until"),
                    ()-> assertNotNull(conference.getInfo(), "info"),
                    ()-> assertEquals("Tallinn", conference.getLocation(), "location")
            );
        }
    }

    @Test
    void availableConferencesFail() {
        registration();
        request = createRequestForConfernces("2024-12-31T10:00:00", "2024-12-31T15:00:00");
        response = controller.availableConferences(request);

        assertAll("Available conferences Fail",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals("No conferences is available at this time period", response.getReason(), "response")
        );

        request = createRequestForConfernces("2024-12-31T10:00:00", "2024-12-31T09:59:59");
        response = controller.availableConferences(request);

        assertAll("Available conferences Fail",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals("Requested time isn't logical", response.getReason(), "response")
        );

        request = createRequestForConfernces("2022-12-31T10:00:00", "2024-12-31T09:59:59");
        response = controller.availableConferences(request);

        assertAll("Available conferences Fail",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals("Requested time isn't logical", response.getReason(), "response")
        );
    }

    private void mockConferenceList(boolean valid) {
        if (valid) {
            when(conferenceDAO.findAllAvailableBetween(any(), any())).thenReturn(getConferenceList());
        } else {
            when(conferenceDAO.findAllAvailableBetween(any(), any())).thenReturn(new ArrayList<>());
        }
    }

    private void mockLocation() {
        when(roomDAO.getRoomLocationByRoomId(any())).thenReturn("Tallinn");
    }

    private void mockFeedback(int result) {
        doReturn(result).when(participantDAO).feedback(any(), any());
        when(participantDAO.feedback(any(), any())).thenReturn(result);
    }

    private void mockConferenceForCancel(UUID response) {
        when(conferenceDAO.isAvailableForCancel(any())).thenReturn(response);
    }

    private void mockParticipantForCancel(int response) {
        when(conferenceDAO.cancelRegistration(any())).thenReturn(response);
    }

    private void mockRegistration() {
        when(conferenceDAO.registerParticipant(any(), any())).thenReturn(1);
    }
}
