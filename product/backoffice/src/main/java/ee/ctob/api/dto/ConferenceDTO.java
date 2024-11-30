package ee.ctob.api.dto;


import ee.ctob.api.Response;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class ConferenceDTO {
    UUID roomUUID;
    UUID conferenceUUID;
    UUID validationUUID;
    UUID oldValidationUUID;
    String info;
    LocalDateTime bookedFrom;
    LocalDateTime bookedUntil;
    List<UUID> participants;
    Integer availableSpace;
    Integer roomCapacity;
    Integer participantsCount;
    List<Response.Feedback> feedbackList;
}
