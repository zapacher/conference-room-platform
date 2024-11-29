package ee.ctob.api.controller;

import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.dto.ParticipantDTO;
import ee.ctob.api.groups.ConferenceAvailable;
import ee.ctob.api.groups.Feedback;
import ee.ctob.api.groups.Registration;
import ee.ctob.api.groups.RegistrationCancel;
import ee.ctob.service.ParticipantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController("conference")
@RequestMapping(
        path = "/conference",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
)
public class ConferenceController {
    @Autowired
    ParticipantService participantService;

    @PostMapping("/registration")
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

    @PostMapping("/registration/cancel")
    public Response registrationCancel(@Validated(RegistrationCancel.class) @RequestBody Request request) {
        ParticipantDTO result = participantService.registrationCancel(
                ParticipantDTO.builder()
                        .build()
        );

        return Response.builder()
                .registrationCancel(result.isRegistrationCancel())
                .build();
    }

    @PostMapping("/feedback")
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
//
//    @PostMapping("/conference/available")
//    public Response conferenceAvailable(@Validated(ConferenceAvailable.class) @RequestBody Request request) {
//        ParticipantDTO result = participantService.availableConferences(
//                ParticipantDTO.builder()
//                        .from(request.getFrom())
//                        .until(request.getUntil())
//                        .build()
//        );
//
//        return Response.builder()
//                .conferenceAvailableList(result.getConferenceAvailableList())
//                .build();
//    }
}
