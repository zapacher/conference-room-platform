package ee.ctob.api.dto;

import ee.ctob.api.Response;
import ee.ctob.data.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ParticipantDTO {
    Instant created;
    UUID validationUUID;
    String firstName;
    String lastName;
    String email;
    Gender gender;
    LocalDateTime dateOfBirth;
    UUID conferenceUUID;
    String feedback;
    String info;
    LocalDateTime from;
    LocalDateTime until;
    boolean feedbackResult;
    boolean registrationCancel;
    List<Response.ConferenceAvailable> conferenceAvailableList;
}
