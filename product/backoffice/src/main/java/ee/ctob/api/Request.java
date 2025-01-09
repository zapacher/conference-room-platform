package ee.ctob.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import ee.ctob.api.groups.*;
import ee.ctob.data.enums.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class Request {
    @Schema(example = "Room name")
    @NotEmpty(groups = RoomCreate.class)
    String name;
    @Schema(example = "Tartu mnt. 62, floor 30, room 247")
    @NotEmpty(groups = RoomCreate.class)
    String location;
    @Schema(example = "60")
    @NotNull(groups = RoomCreate.class)
    Integer capacity;
    @Schema(example = "AVAILABLE|CLOSED")
    RoomStatus status;
    @Schema(example = "2d790a4d-7c9c-4e23-9c9c-5749c5fa7fdb")
    @NotNull(groups = ConferenceCreate.class)
    UUID roomUUID;
    @Schema(example = "Room description, has seats, table, cooler, air conditioner etc.")
    @NotEmpty(groups = {ConferenceCreate.class, ConferenceUpdate.class})
    String description;
    @Schema(example = "2024-12-30T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(groups = {ConferenceCreate.class, ConferenceUpdate.class})
    LocalDateTime from;
    @Schema(example = "2024-12-30T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(groups = {ConferenceCreate.class, ConferenceUpdate.class})
    LocalDateTime until;
    @Schema(example = "2d790a4d-7c9c-4e23-9c9c-5749c5fa7fdb")
    @NotNull(groups = {ConferenceCancel.class, ConferenceUpdate.class, RoomUpdate.class, ConferenceFeedbacks.class, ConferenceSpace.class})
    UUID validationUUID;
}
