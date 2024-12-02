package ee.ctob.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import ee.ctob.api.groups.*;
import ee.ctob.data.enums.RoomStatus;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class Request {
    @NotEmpty(groups = RoomCreate.class)
    String name;
    @NotEmpty(groups = RoomCreate.class)
    String location;
    @NotNull(groups = RoomCreate.class)
    Integer capacity;
    RoomStatus status;
    @NotNull(groups = ConferenceCreate.class)
    UUID roomUUID;
    @NotEmpty(groups = {ConferenceCreate.class, ConferenceUpdate.class})
    String description;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(groups = {ConferenceCreate.class, ConferenceUpdate.class})
    LocalDateTime from;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(groups = {ConferenceCreate.class, ConferenceUpdate.class})
    LocalDateTime until;
    @NotNull(groups = {ConferenceCancel.class, ConferenceUpdate.class, RoomUpdate.class, ConferenceFeedbacks.class, ConferenceSpace.class})
    UUID validationUUID;
    UUID newRoomUUID;
}
