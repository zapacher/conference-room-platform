package ee.ctob.data;

import ee.ctob.data.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "participants")
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    LocalDateTime created;
    @Column(name = "validation_uuid",unique = true)
    UUID validationUUID;
    @Column(name = "participant_uuid",unique = true)
    UUID participantUUID;
    String firstName;
    String lastName;
    String email;
    @Enumerated(EnumType.STRING)
    Gender gender;
    LocalDateTime dateOfBirth;
    String feedback;
}
