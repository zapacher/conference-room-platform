package ee.ctob.api.controller;

import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.dto.ConferenceDTO;
import ee.ctob.api.dto.RoomDTO;
import ee.ctob.api.groups.*;
import ee.ctob.api.mapper.ConferenceMapper;
import ee.ctob.api.mapper.RoomMapper;
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


    ConferenceMapper conferenceMapper = ConferenceMapper.INSTANCE;
    RoomMapper roomMapper = RoomMapper.INSTANCE;


    @Operation(summary = "Register new room. Autoconfigured to AVAILABLE")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "name, capacity, location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "validationUUID, roomUUID will be in response if success, reason will be in response if error"),
            @ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")
    })
    @PostMapping("/room/create")
    public Response roomCreate(@Validated (RoomCreate.class) @RequestBody Request request) {
        RoomDTO result = roomService.create(roomMapper.toRoomDTO(request));

        return roomMapper.toResponse(result);
    }

    @Operation(summary = "Update room capacity OR status")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID, capacity OR status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "roomUUID, roomCapacity, roomStatus, validationUUID will be in response if success, reason will be in response if error"),
            @ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")
    })
    @PostMapping("/room/update")
    public Response roomUpdate(@Validated (RoomUpdate.class) @RequestBody Request request) {
        RoomDTO result = roomService.update(roomMapper.toRoomDTO(request));

        return roomMapper.toResponse(result);
    }

    @Operation(summary = "Create new conference. Autoconfigured to AVAILABLE")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "roomUUID, from, until, description(optional)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "conferenceUUID, validationUUID, bookedFrom, bookedUntil will be in response if success, reason will be in response if error"),
            @ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")
    })
    @PostMapping("/conference/create")
    public Response conferenceCreate(@Validated (ConferenceCreate.class) @RequestBody Request request) {
        ConferenceDTO result = conferenceService.create(conferenceMapper.toConferenceDTO(request));

        return conferenceMapper.toResponse(result);
    }

    @Operation(summary = "Conference update room or/and time")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID, from, until, roomUUID(if changing room), description(optional)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "roomUUID, conferenceUUID, validationUUID, bookedFrom, bookedUntil will be in response if success, reason will be in response if error"),
            @ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")
    })
    @PostMapping("/conference/update")
    public Response conferenceUpdate(@Validated (ConferenceUpdate.class) @RequestBody Request request) {
        ConferenceDTO result = conferenceService.update(conferenceMapper.toConferenceDTO(request));

        return conferenceMapper.toResponse(result);
    }

    @Operation(summary = "Conference info of space")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "validationUUID, availableSpace, roomCapacity, participantsCount will be in response if success, reason will be in response if error"),
            @ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")
    })
    @PostMapping("/conference/space")
    public Response conferenceSpace(@Validated (ConferenceSpace.class) @RequestBody Request request) {
        ConferenceDTO result = conferenceService.checkFreeSpace(conferenceMapper.toConferenceDTO(request));

        return conferenceMapper.toResponse(result);
    }

    @Operation(summary = "Feedbacks for conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "validationUUID, feedbackList[shortName,feedback] will be in response if success, reason will be in response if error"),
            @ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")
    })
    @PostMapping("/conference/feedback")
    public Response conferenceFeedbacks(@Validated (ConferenceFeedbacks.class) @RequestBody Request request) {
        ConferenceDTO result = conferenceService.feedbackList(conferenceMapper.toConferenceDTO(request));

        return conferenceMapper.toResponse(result);
    }

    @Operation(summary = "Close/cancel conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "validationUUID will be in response if success, reason will be in response if error"),
            @ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")
    })
    @PostMapping("/conference/cancel")
    public Response conferenceCancel(@Validated (ConferenceCancel.class) @RequestBody Request request) {
        ConferenceDTO result = conferenceService.cancel(conferenceMapper.toConferenceDTO(request));

        return conferenceMapper.toResponse(result);
    }
}
