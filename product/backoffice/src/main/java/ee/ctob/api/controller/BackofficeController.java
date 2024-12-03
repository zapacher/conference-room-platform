package ee.ctob.api.controller;

import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.dto.ConferenceDTO;
import ee.ctob.api.dto.RoomDTO;
import ee.ctob.api.groups.*;
import ee.ctob.services.ConferenceService;
import ee.ctob.services.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "Register new room. Autoconfigured to AVAILABLE")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "name, capacity, location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "validationUUID, roomUUID will be in response if success")
    })
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

    @Operation(summary = "Update room capacity OR status")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID, capacity OR status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "roomUUID, roomCapacity, roomStatus, validationUUID will be in response if success"),
            @ApiResponse(responseCode = "200", description = "reason will be in response if error"),
    })
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

    @Operation(summary = "Create new conference. Autoconfigured to AVAILABLE")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "roomUUID, from, until, description(optional)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "conferenceUUID, validationUUID, bookedFrom, bookedUntil will be in response if success"),
            @ApiResponse(responseCode = "200", description = "reason will be in response if error"),
    })
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

    @Operation(summary = "Conference update room or/and time")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID, from, until, roomUUID(if changing room), description(optional)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "roomUUID, conferenceUUID, validationUUID, bookedFrom, bookedUntil, oldValidationUUID will be in response if success"),
            @ApiResponse(responseCode = "200", description = "reason will be in response if error"),
    })
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

    @Operation(summary = "Conference info of space")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "validationUUID, availableSpace, roomCapacity, participantsCount will be in response if success"),
            @ApiResponse(responseCode = "200", description = "reason will be in response if error"),
    })
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

    @Operation(summary = "Feedbacks for conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "validationUUID, feedbackList[shortName,feedback] will be in response if success"),
            @ApiResponse(responseCode = "200", description = "reason will be in response if error"),
    })
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

    @Operation(summary = "Close/cancel conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "validationUUID will be in response if success"),
            @ApiResponse(responseCode = "200", description = "reason will be in response if error"),
    })
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
