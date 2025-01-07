package ee.ctob.api.dto;

import ee.ctob.api.enums.RoomStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RoomDTO {
    String name;
    RoomStatus status;
    String location;
    Integer capacity;
    UUID roomUUID;
    LocalDateTime from;
    LocalDateTime until;
    String description;
    UUID validationUUID;
    UUID conferenceUUID;
}
