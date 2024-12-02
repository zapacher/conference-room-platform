package ee.ctob.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.ctob.data.enums.ConferenceStatus;
import ee.ctob.data.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Data
    @Builder
    public static class Feedback {
        String shortName;
        String feedback;
    }
}
