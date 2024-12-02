package controller;


import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.controller.ConferenceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;
import testutils.TestContainer;

import java.util.UUID;

@Testcontainers
class TestConferenceController extends TestContainer {


    @Autowired
    private ConferenceController controller;

    private UUID roomUUID;
    private UUID roomValidationUUID;
    private UUID conferenceUUID;
    private UUID conferenceValidationUUID;
    private Request request;
    private Response response;
    private boolean withoutRoom = false;

}