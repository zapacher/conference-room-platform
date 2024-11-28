package ee.ctob.api;

import ee.ctob.api.groups.ConferenceAvailable;
import ee.ctob.api.groups.Feedback;
import ee.ctob.api.groups.Registration;
import ee.ctob.api.groups.RegistrationCancel;
import ee.ctob.data.enums.Gender;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Value
public class Request {
    @NotNull(groups = ConferenceAvailable.class)
    LocalDateTime from;
    @NotNull(groups = ConferenceAvailable.class)
    LocalDateTime until;
    @NotNull(groups = Registration.class)
    String firstname;
    @NotNull(groups = Registration.class)
    String lastName;
    @NotNull(groups = Registration.class)
    Gender gender;
    @NotNull(groups = Registration.class)
    String email;
    @NotNull(groups = Registration.class)
    LocalDateTime dateOfBirth;
    @NotNull(groups = Registration.class)
    UUID conferenceUUID;
    @NotNull(groups = {RegistrationCancel.class, Feedback.class})
    UUID validationUUID;
    @NotNull(groups = Feedback.class)
    String feedback;
}
