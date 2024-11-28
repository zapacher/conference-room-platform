package ee.ctob.api;

import ee.ctob.api.groups.*;
import ee.ctob.data.enums.RoomStatus;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class Request {
    @NotNull(groups = RoomCreate.class)
    String name;
    @NotNull(groups = RoomCreate.class)
    String location;
    @NotNull(groups = RoomCreate.class)
    Integer capacity;
    RoomStatus status;
    @NotNull(groups = ConferenceCreate.class)
    UUID roomUUID;
    @NotNull(groups = ConferenceCreate.class)
    String description;
    @NotNull(groups = {ConferenceCreate.class, ConferenceUpdate.class})
    LocalDateTime from;
    @NotNull(groups = {ConferenceCreate.class, ConferenceUpdate.class})
    LocalDateTime until;
    @NotNull(groups = {ConferenceCancel.class, ConferenceUpdate.class, RoomUpdate.class, ConferenceFeedbacks.class, ConferenceSpace.class})
    UUID validationUUID;
    UUID newRoomUUID;
}
