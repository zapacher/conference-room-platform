package ee.ctob.controller;

import ee.ctob.access.ConferenceDAO;
import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.controller.ConferenceController;
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

import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void participantRegistration() {
        request = createRegitrationRequest(UUID.randomUUID());
        response = controller.registration(request);
        assertAll("Registration success",
                ()-> assertNotNull(response, "Response")
        );
        System.out.println(response);
    }
}
