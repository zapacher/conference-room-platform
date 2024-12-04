package ee.ctob.api.controller;

import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.dto.ParticipantDTO;
import ee.ctob.api.groups.ConferenceAvailable;
import ee.ctob.api.groups.Feedback;
import ee.ctob.api.groups.Registration;
import ee.ctob.api.groups.RegistrationCancel;
import ee.ctob.api.mapper.ParticipantMapper;
import ee.ctob.service.ParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController("conference")
@RequestMapping(
        path = "/conference",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class ConferenceController {

    final ParticipantService participantService;

    ParticipantMapper mapper = ParticipantMapper.INSTANCE;

    @Operation(summary = "Register new participant to conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "firstname, lastname, gender, email, dateOfBirth, conferenceUUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "100", description = "Any precondition errors will be explained"),
            @ApiResponse(responseCode = "200", description = "validationUUID will be in response if success"),
            @ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")
    })
    @PostMapping("/registration/create")
    public Response registration(@Validated(Registration.class) @RequestBody Request request) {
        ParticipantDTO result = participantService.registration(mapper.toParticipantDTO(request));
        return mapper.toResponse(result);
    }

    @Operation(summary = "Cancel registration")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "100", description = "Any precondition errors will be explained"),
            @ApiResponse(responseCode = "200", description = "registrationCancel = true will be in response if success"),
            @ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")
    })
    @PostMapping("/registration/cancel")
    public Response registrationCancel(@Validated(RegistrationCancel.class) @RequestBody Request request) {
        ParticipantDTO result = participantService.registrationCancel(mapper.toParticipantDTO(request));
        return mapper.toResponse(result);
    }

    @Operation(summary = "Leave feedback after conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID, feedback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "100", description = "Any precondition errors will be explained"),
            @ApiResponse(responseCode = "200", description = "feedbackResult = true will be in response if success"),
            @ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")
    })
    @PostMapping("/feedback/create")
    public Response feedback(@Validated(Feedback.class) @RequestBody Request request) {
        ParticipantDTO result = participantService.feedback(mapper.toParticipantDTO(request));
        return mapper.toResponse(result);
    }

    @Operation(summary = "Leave feedback after conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "from, until")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "100", description = "Any precondition errors will be explained"),
            @ApiResponse(responseCode = "200", description = "List of schema ConferenceAvailable response if success"),
            @ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")
    })
    @PostMapping("/available")
    public Response availableConferences(@Validated(ConferenceAvailable.class) @RequestBody Request request) {
        ParticipantDTO result = participantService.availableConferences(mapper.toParticipantDTO(request));
        return mapper.toResponse(result);
    }
}
