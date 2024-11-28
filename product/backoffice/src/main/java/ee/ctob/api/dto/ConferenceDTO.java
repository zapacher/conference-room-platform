package ee.ctob.api.dto;


import ee.ctob.api.Feedback;
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
    String info;
    LocalDateTime bookedFrom;
    LocalDateTime bookedUntil;
    List<UUID> participants;
    Integer availableSpace;
    Integer roomCapacity;
    Integer participantsCount;
    List<Feedback> feedbackList;
}
