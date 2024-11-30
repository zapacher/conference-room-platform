package ee.ctob.data;

import ee.ctob.data.enums.ConferenceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conferences", schema = "backoffice")
public class Conference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(name = "conference_uuid", unique = true)
    UUID conferenceUUID;
    @Column(name = "validation_uuid", unique = true)
    UUID validationUUID;
    @Column(name = "room_uuid")
    UUID roomUUID;
    @Enumerated(EnumType.STRING)
    ConferenceStatus status;
    String info;
    LocalDateTime bookedFrom;
    LocalDateTime bookedUntil;
    @ElementCollection
    @CollectionTable(
            name = "conference_participants", schema = "backoffice",
            joinColumns = @JoinColumn(name = "conference_id")
    )
    @Column(name = "participant_uuid")
    List<UUID> participants = new ArrayList<>();
}
