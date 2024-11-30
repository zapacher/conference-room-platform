package ee.ctob.api;

import ee.ctob.api.groups.ConferenceAvailable;
import ee.ctob.api.groups.Feedback;
import ee.ctob.api.groups.Registration;
import ee.ctob.api.groups.RegistrationCancel;
import ee.ctob.data.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class Request {
    @NotNull(groups = ConferenceAvailable.class)
    @Schema(example = "2024-12-28T11:00:00")
    LocalDateTime from;
    @Schema(example = "2024-12-28T12:00:00")
    @NotNull(groups = ConferenceAvailable.class)
    LocalDateTime until;
    @Schema(example = "Chuck")
    @NotEmpty(groups = Registration.class)
    String firstname;
    @Schema(example = "Norris")
    @NotEmpty(groups = Registration.class)
    String lastName;
    @Schema(example = "MALE")
    @NotNull(groups = Registration.class)
    Gender gender;
    @Schema(example = "chuck.norris@gmail.com")
    @NotNull(groups = Registration.class)
    @Email
    String email;
    @Schema(example = "1980-12-20T0:00:00")
    @Past(groups = Registration.class)
    @NotNull(groups = Registration.class)
    LocalDateTime dateOfBirth;
    @Schema(example = "af7c1fe6-d669-414e-b066-e9733f0de7a8")
    @NotNull(groups = Registration.class)
    UUID conferenceUUID;
    @Schema(example = "08c71152-c552-42e7-b094-f510ff44e9cb")
    @NotNull(groups = {RegistrationCancel.class, Feedback.class})
    UUID validationUUID;
    @Schema(example = "Simple text")
    @NotEmpty(groups = Feedback.class)
    String feedback;
}
