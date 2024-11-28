package ee.ctob.data;

import ee.ctob.data.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rooms", schema = "backoffice")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Enumerated(EnumType.STRING)
    RoomStatus status;
    @Column(name = "room_uuid", unique = true)
    UUID roomUUID;
    String name;
    @Column(name = "validation_uuid", unique = true)
    UUID validationUuid;
    Integer capacity;
    String location;
}
