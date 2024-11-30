package ee.ctob.api.controller;

import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.dto.ConferenceDTO;
import ee.ctob.api.dto.RoomDTO;
import ee.ctob.api.groups.*;
import ee.ctob.services.ConferenceService;
import ee.ctob.services.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ee.ctob.data.enums.ConferenceStatus.CANCELED;

@Slf4j
@RestController("backoffice")
@RequestMapping(
        path = "/backoffice",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
)
public class BackofficeController {

    @Autowired
    RoomService roomService;
    @Autowired
    ConferenceService conferenceService;

    @PostMapping("/room/create")
    public Response roomCreate(@Validated (RoomCreate.class) @RequestBody Request request) {
        RoomDTO result = roomService.create(RoomDTO.builder()
                .name(request.getName())
                .capacity(request.getCapacity())
                .location(request.getLocation())
                .build());

        return Response.builder()
                .validationUUID(result.getValidationUUID())
                .roomUUID(result.getRoomUUID())
                .build();
    }

    @PostMapping("/room/update")
    public Response roomUpdate(@Validated (RoomUpdate.class) @RequestBody Request request) {
        RoomDTO result = roomService.update(RoomDTO.builder()
                .validationUUID(request.getValidationUUID())
                .capacity(request.getCapacity())
                .status(request.getStatus())
                .build());

        return Response.builder()
                .roomUUID(result.getRoomUUID())
                .roomCapacity(result.getCapacity())
                .roomStatus(result.getStatus())
                .validationUUID(result.getValidationUUID())
                .reason(result.getDescription())
                .build();
    }

    @PostMapping("/conference/create")
    public Response conferenceCreate(@Validated (ConferenceCreate.class) @RequestBody Request request) {
        ConferenceDTO result = conferenceService.create(
                ConferenceDTO.builder()
                        .roomUUID(request.getRoomUUID())
                        .info(request.getDescription())
                        .bookedFrom(request.getFrom())
                        .bookedUntil(request.getUntil())
                        .build()
        );

        return Response.builder()
                .conferenceUUID(result.getConferenceUUID())
                .validationUUID(result.getValidationUUID())
                .bookedFrom(result.getBookedFrom())
                .bookedUntil(result.getBookedUntil())
                .reason(result.getInfo())
                .build();
    }

    @PostMapping("/conference/update")
    public Response conferenceUpdate(@Validated (ConferenceUpdate.class) @RequestBody Request request) {
        ConferenceDTO result = conferenceService.update(
                ConferenceDTO.builder()
                        .validationUUID(request.getValidationUUID())
                        .roomUUID(request.getRoomUUID())
                        .bookedFrom(request.getFrom())
                        .bookedUntil(request.getUntil())
                        .info(request.getDescription())
                        .build()
        );

        return Response.builder()
                .roomUUID(result.getRoomUUID())
                .conferenceUUID(result.getConferenceUUID())
                .validationUUID(result.getValidationUUID())
                .bookedFrom(result.getBookedFrom())
                .bookedUntil(result.getBookedUntil())
                .oldValidationUUID(result.getOldValidationUUID())
                .reason(result.getInfo())
                .build();
    }

    @PostMapping("/conference/space")
    public Response conferenceSpace(@Validated (ConferenceSpace.class) @RequestBody Request request) {
        ConferenceDTO result = conferenceService.checkFreeSpace(
                ConferenceDTO.builder()
                        .validationUUID(request.getValidationUUID())
                        .build()
        );
        return Response.builder()
                .validationUUID(result.getValidationUUID())
                .availableSpace(result.getAvailableSpace())
                .roomCapacity(result.getRoomCapacity())
                .participantsCount(result.getParticipantsCount())
                .reason(result.getInfo())
                .build();
    }

    @PostMapping("/conference/feedback")
    public Response conferenceFeedbacks(@Validated (ConferenceFeedbacks.class) @RequestBody Request request) {
        ConferenceDTO result = conferenceService.feedbackList(
                ConferenceDTO.builder()
                        .validationUUID(request.getValidationUUID())
                        .build()
        );
        return Response.builder()
                .validationUUID(result.getValidationUUID())
                .feedbackList(result.getFeedbackList())
                .reason(result.getInfo())
                .build();
    }

    @PostMapping("/conference/cancel")
    public Response conferenceCancel(@Validated (ConferenceCancel.class) @RequestBody Request request) {
        ConferenceDTO result = conferenceService.cancel(
                ConferenceDTO.builder()
                        .validationUUID(request.getValidationUUID())
                        .build()
        );

        return Response.builder()
                .validationUUID(result.getOldValidationUUID())
                .reason(result.getInfo())
                .build();
    }
}
