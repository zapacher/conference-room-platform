package ee.ctob.data.kotlin

import ee.ctob.data.enums.Gender
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "participants", schema = "conference")
data class Participant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    val created: LocalDateTime,
    @Column(name = "validation_uuid", unique = true)
    val validationUUID: UUID,
    @Column(name = "participant_uuid", unique = true)
    val participantUUID: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    @Enumerated(EnumType.STRING)
    val gender: Gender,
    val dateOfBirth: LocalDateTime,
    val feedback: String? = null
)