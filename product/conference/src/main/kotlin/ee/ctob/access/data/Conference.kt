//package ee.ctob.access.data
//
//import ee.ctob.api.enums.ConferenceStatus
//import java.time.LocalDateTime
//import java.util.*
//import javax.persistence.*
//import kotlin.collections.mutableListOf
//
//@Entity
//@Table(name = "conferences", schema = "backoffice")
//data class Conference(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    val id: Int? = null, // ID is nullable and auto-generated
//    @Column(name = "conference_uuid", unique = true)
//    val conferenceUUID: UUID? = null,
//    @Column(name = "validation_uuid", unique = true)
//    val validationUUID: UUID? = null,
//    @Column(name = "room_uuid")
//    val roomUUID: UUID? = null,
//    @Enumerated(EnumType.STRING)
//    val status: ConferenceStatus? = null,
//    val info: String? = null,
//    val bookedFrom: LocalDateTime? = null,
//    val bookedUntil: LocalDateTime? = null,
//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(
//        name = "conference_participants", schema = "backoffice",
//        joinColumns = [JoinColumn(name = "conference_id")]
//    )
//    @Column(name = "participant_uuid")
//    val participants: List<UUID>? = mutableListOf() // Mutable list for participants
//)