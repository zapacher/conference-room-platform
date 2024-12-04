package ee.ctob.controller;

import ee.ctob.access.ConferenceDAO;
import ee.ctob.access.ParticipantDAO;
import ee.ctob.access.RoomDAO;
import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.controller.ConferenceController;
import ee.ctob.api.error.BadRequestException;
import ee.ctob.api.error.ErrorResponse;
import ee.ctob.api.error.PreconditionsFailedException;
import ee.ctob.data.Conference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import testutils.TestContainer;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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

    private Request request;
    private Response response;
    private ErrorResponse errorResponse;
    private UUID participantValidationUUID;

    @Test
    void registration() {
        request = createRegitrationRequest(UUID.randomUUID());
        mockRegistration();
        response = controller.registration(request);
        assertAll("Registration Success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNotNull(response.getValidationUUID(), "validationUUID")
        );

        participantValidationUUID = response.getValidationUUID();
    }

    @Test
    void registrationFail() {
        request = createRegitrationRequest(UUID.randomUUID());
        errorResponse = assertThrows(
                PreconditionsFailedException.class,
                () -> controller.registration(request)
        ).getError();
        assertAll("Registration Fail",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Registration isn't available for this conference", errorResponse.getMessage(), "error message")
        );
    }

    @Test
    void registrationCancel() {
        registration();

        mockConferenceForCancel();
        mockParticipantForCancel(1);

        request = createRegitrationCancelRequest(participantValidationUUID);
        response = controller.registrationCancel(request);

        assertAll("Registration cancel success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertTrue(response.isRegistrationCancel(), "registrationCancel")
        );
    }

    @Test
    void registrationCancelFail() {
        registration();

        request = createRegitrationCancelRequest(UUID.randomUUID());

        errorResponse = assertThrows(
                PreconditionsFailedException.class,
                () -> controller.registrationCancel(request)
        ).getError();
        assertAll("Registration cancel Fail",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Participant with this validation doesn't exists", errorResponse.getMessage(), "error message")
        );

        request = createRegitrationCancelRequest(participantValidationUUID);
        mockConferenceForCancelThrow();

        errorResponse = assertThrows(
                PreconditionsFailedException.class,
                () -> controller.registrationCancel(request)
        ).getError();
        assertAll("Registration cancel Fail",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Conference already started or finished", errorResponse.getMessage(), "error message")
        );


        request = createRegitrationCancelRequest(participantValidationUUID);
        mockConferenceForCancelThrow();
        mockParticipantForCancel(0);

        errorResponse = assertThrows(
                PreconditionsFailedException.class,
                () -> controller.registrationCancel(request)
        ).getError();
        assertAll("Registration cancel Fail",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Conference already started or finished", errorResponse.getMessage(), "error message")
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
                ()-> assertTrue(response.isFeedbackResult(), "feedbackResult")
        );
    }

    @Test
    void feedbackFail() {
        registration();

        request = createFeedbackRequest(participantValidationUUID, "Any text for feedback");
        mockFeedback(0);
        errorResponse = assertThrows(
                PreconditionsFailedException.class,
                () -> controller.feedback(request)
        ).getError();
        assertAll("Feeback Fail",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Feedback already exists or conference isn't finished", errorResponse.getMessage(), "error message")
        );
    }

    @Test
    void availableConferences() {
        registration();

        mockConferenceList();
        mockLocation();

        request = createRequestForConfernces("2024-12-10T15:00:00", "2024-12-31T15:00:00");
        response = controller.availableConferences(request);

        assertAll("Available conferences",
                ()-> assertNotNull(response, "Response"),
                ()-> assertEquals(5, response.getConferenceAvailableList().size(), "conferenceAvailableList")
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
        errorResponse = assertThrows(
                PreconditionsFailedException.class,
                () -> controller.availableConferences(request)
        ).getError();
        assertAll("Available conferences Fail",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(100, errorResponse.getCode(), "error code"),
                ()-> assertEquals("No conferences is available at this time period", errorResponse.getMessage(), "error message")
        );

        request = createRequestForConfernces("2024-12-31T10:00:00", "2024-12-31T09:59:59");
        errorResponse = assertThrows(
                BadRequestException.class,
                () -> controller.availableConferences(request)
        ).getError();
        assertAll("Available conferences Fail",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(400, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Requested time isn't logical", errorResponse.getMessage(), "error message")
        );

        request = createRequestForConfernces("2022-12-31T10:00:00", "2024-12-31T09:59:59");
        errorResponse = assertThrows(
                BadRequestException.class,
                () -> controller.availableConferences(request)
        ).getError();
        assertAll("Available conferences Fail",
                ()-> assertNotNull(errorResponse, "ErrorResponse"),
                ()-> assertEquals(400, errorResponse.getCode(), "error code"),
                ()-> assertEquals("Requested time isn't logical", errorResponse.getMessage(), "error message")
        );
    }

    private void mockConferenceList() {
        when(conferenceDAO.findAllAvailableBetween(any(), any())).thenReturn(getConferenceList())
    }

    private void mockLocation() {
        when(roomDAO.getRoomLocationByRoomId(any())).thenReturn("Tallinn");
    }

    private void mockFeedback(int result) {
        doReturn(result).when(participantDAO).feedback(any(), any());
        when(participantDAO.feedback(any(), any())).thenReturn(result);
    }

    private void mockConferenceForCancel() {
        when(conferenceDAO.isAvailableForCancel(any())).thenReturn(Optional.of(Conference.builder().conferenceUUID(UUID.randomUUID()).build()));
    }

    private void mockConferenceForCancelThrow() {
        doThrow(new PreconditionsFailedException("Conference already started or finished"))
                .when(conferenceDAO).isAvailableForCancel(any());
    }

    private void mockParticipantForCancel(int response) {
        when(conferenceDAO.cancelRegistration(any())).thenReturn(response);
    }

    private void mockRegistration() {
        when(conferenceDAO.registerParticipant(any(), any())).thenReturn(1);
    }
}
