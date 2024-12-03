package ee.ctob.api.controller;

import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.dto.ParticipantDTO;
import ee.ctob.api.groups.ConferenceAvailable;
import ee.ctob.api.groups.Feedback;
import ee.ctob.api.groups.Registration;
import ee.ctob.api.groups.RegistrationCancel;
import ee.ctob.service.ParticipantService;
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


@RestController("conference")
@RequestMapping(
        path = "/conference",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
)
public class ConferenceController {
    @Autowired
    ParticipantService participantService;

    @Operation(summary = "Register new participant to conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "firstName, lastName, gender, email, dateOfBirth, conferenceUUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "validationUUID will be in response if success"),
            @ApiResponse(responseCode = "200", description = "reason will be in response if error")
    })
    @PostMapping("/registration/create")
    public Response registration(@Validated(Registration.class) @RequestBody Request request) {
        ParticipantDTO result = participantService.registration(
                ParticipantDTO.builder()
                        .firstName(request.getFirstname().trim())
                        .lastName(request.getLastName().trim())
                        .gender(request.getGender())
                        .email(request.getEmail())
                        .dateOfBirth(request.getDateOfBirth())
                        .conferenceUUID(request.getConferenceUUID())
                        .build()
        );

        return Response.builder()
                .validationUUID(result.getValidationUUID())
                .reason(result.getInfo())
                .build();
    }

    @Operation(summary = "Cancel registration")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "registrationCancel = true will be in response if success"),
            @ApiResponse(responseCode = "200", description = "reason will be in response if error")
    })
    @PostMapping("/registration/cancel")
    public Response registrationCancel(@Validated(RegistrationCancel.class) @RequestBody Request request) {
        ParticipantDTO result = participantService.registrationCancel(
                ParticipantDTO.builder()
                        .validationUUID(request.getValidationUUID())
                        .build()
        );

        return Response.builder()
                .registrationCancel(result.isRegistrationCancel())
                .reason(result.getInfo())
                .build();
    }
    @Operation(summary = "Leave feedback after conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID, feedback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "feedbackResult = true will be in response if success"),
            @ApiResponse(responseCode = "200", description = "reason will be in response if error")
    })
    @PostMapping("/feedback/create")
    public Response feedback(@Validated(Feedback.class) @RequestBody Request request) {
        ParticipantDTO result = participantService.feedback(
                ParticipantDTO.builder()
                        .validationUUID(request.getValidationUUID())
                        .feedback(request.getFeedback())
                        .build()
        );

        return Response.builder()
                .validationUUID(result.getValidationUUID())
                .feedbackResult(result.isFeedbackResult())
                .reason(result.getInfo())
                .build();
    }

    @Operation(summary = "Leave feedback after conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "from, until")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of schema ConferenceAvailable response if success"),
            @ApiResponse(responseCode = "200", description = "reason will be in response if error")
    })
    @PostMapping("/available")
    public Response availableConferences(@Validated(ConferenceAvailable.class) @RequestBody Request request) {
        ParticipantDTO result = participantService.availableConferences(
                ParticipantDTO.builder()
                        .from(request.getFrom())
                        .until(request.getUntil())
                        .build()
        );

        return Response.builder()
                .conferenceAvailableList(result.getConferenceAvailableList())
                .reason(result.getInfo())
                .build();
    }
}
