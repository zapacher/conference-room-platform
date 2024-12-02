package ee.ctob.controller;

import ee.ctob.access.ConferenceDAO;
import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.controller.ConferenceController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import testutils.TestContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static testutils.ObjectCreators.createRegitrationRequest;

@Testcontainers
@ExtendWith(MockitoExtension.class)
public class UnitTestsConferenceController extends TestContainer {
    @MockBean
    ConferenceDAO conferenceDAO;

    @InjectMocks
    @Autowired
    ConferenceController controller;

    Request request;
    Response response;
    UUID conferenceUUID;
    UUID participantValidationUUID;

    @Test
    void participantRegistration() {
        conferenceUUID = UUID.randomUUID();
        request = createRegitrationRequest(conferenceUUID);
        response = controller.registration(request);
        mockRegistration(UUID.randomUUID());
        assertAll("Registration Success",
                ()-> assertNotNull(response, "Response"),
                ()-> assertNotNull(response.getValidationUUID(), "validationUUID"),
                ()-> assertNull(response.getReason(), "reason")
        );
        participantValidationUUID = response.getValidationUUID();
    }

    @Test
    void participantRegistrationFail() {
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

    }

    @Test
    void registrationCancelFail() {

    }

    @Test
    void availableConferences() {

    }

    @Test
    void feedback() {

    }

    private void mockRegistration(UUID participantUUID) {
        when(conferenceDAO.registerParticipant(participantUUID, conferenceUUID)).thenReturn(1);
    }
}
