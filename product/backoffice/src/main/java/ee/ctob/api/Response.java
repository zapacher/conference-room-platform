package ee.ctob.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.ctob.data.enums.ConferenceStatus;
import ee.ctob.data.enums.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(example = "2d790a4d-7c9c-4e23-9c9c-5749c5fa7fdb")
    UUID validationUUID;
    @Schema(example = "2d790a4d-7c9c-4e23-9c9c-5749c5fa7fdb")
    UUID roomUUID;
    @Schema(example = "2d790a4d-7c9c-4e23-9c9c-5749c5fa7fdb")
    UUID conferenceUUID;
    @Schema(example = "53")
    Integer availableSpace;
    @Schema(example = "60")
    Integer roomCapacity;
    @Schema(example = "7")
    Integer participantsCount;
    @Schema(example = "AVAILABLE|CLOSED")
    RoomStatus roomStatus;
    @Schema(example = "2d790a4d-7c9c-4e23-9c9c-5749c5fa7fdb")
    UUID newValidationUUID;
    @Schema(example = "List of feedback schema")
    List<Feedback> feedbackList;
    @Schema(example = "2024-12-30T12:00:00")
    LocalDateTime bookedFrom;
    @Schema(example = "2024-12-30T17:00:00")
    LocalDateTime bookedUntil;
    @Schema(example = "AVAILABLE|CANCELED")
    ConferenceStatus conferenceStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Feedback {
        @Schema(example = "C.N.")
        String shortName;
        @Schema(example = "Feedback text from participant of conference")
        String feedback;
    }
}
