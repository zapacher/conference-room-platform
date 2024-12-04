package ee.ctob.api;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @Schema(example = "true")
    boolean feedbackResult;
    @Schema(example = "true")
    boolean registrationCancel;
    @Schema(example = "List of schema ConferenceAvailable")
    List<ConferenceAvailable> conferenceAvailableList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConferenceAvailable {
        @Schema(example = "5a743569-35f8-4588-899a-7ebcd4a75def")
        UUID conferenceUUID;
        @Schema(example = "Tartu mnt. 62, floor 30, room 247")
        String location;
        @Schema(example = "20")
        Integer participantsAmount;
        @Schema(example = "2024-12-30T12:00:00")
        LocalDateTime from;
        @Schema(example = "2024-12-30T17:30:00")
        LocalDateTime until;
        @Schema(example = "Info about conference")
        String info;
    }
}
