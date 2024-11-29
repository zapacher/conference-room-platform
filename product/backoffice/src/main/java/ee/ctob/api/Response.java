package ee.ctob.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.ctob.data.enums.ConferenceStatus;
import ee.ctob.data.enums.RoomStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    UUID validationUUID;
    UUID oldValidationUUID;
    UUID roomUUID;
    UUID conferenceUUID;
    Integer availableSpace;
    Integer roomCapacity;
    Integer participantsCount;
    RoomStatus roomStatus;
    UUID newValidationUUID;
    List<Feedback> feedbackList;
    LocalDateTime bookedFrom;
    LocalDateTime bookedUntil;
    ConferenceStatus conferenceStatus;
    String reason;
}
