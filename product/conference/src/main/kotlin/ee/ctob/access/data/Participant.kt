//package ee.ctob.access.data
//
//import ee.ctob.api.enums.Gender
//import java.time.LocalDateTime
//import java.util.*
//import javax.persistence.*
//
//@Entity
//@Table(name = "participants", schema = "conference")
//data class Participant(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    val id: Int? = null,
//    val created: LocalDateTime? = null,
//    @Column(name = "validation_uuid", unique = true)
//    val validationUUID: UUID? = null,
//    @Column(name = "participant_uuid", unique = true)
//    val participantUUID: UUID? = null,
//    val firstName: String? = null,
//    val lastName: String? = null,
//    val email: String? = null,
//    @Enumerated(EnumType.STRING)
//    val gender: Gender? = null,
//    val dateOfBirth: LocalDateTime? = null,
//    val feedback: String? = null
//)