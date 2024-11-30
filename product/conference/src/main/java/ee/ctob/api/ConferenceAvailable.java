package ee.ctob.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ConferenceAvailable {
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

}
