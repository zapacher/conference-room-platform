//package ee.ctob.access.data
//
//import ee.ctob.api.enums.RoomStatus
//import java.util.UUID
//import javax.persistence.*
//
//@Entity
//@Table(name = "rooms", schema = "backoffice")
//data class Room(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    val id: Int? = null, // nullable for auto-generated ids
//    @Enumerated(EnumType.STRING)
//    val status: RoomStatus,
//    @Column(name = "room_uuid", unique = true)
//    val roomUUID: UUID,
//    val name: String,
//    @Column(name = "validation_uuid", unique = true)
//    val validationUuid: UUID,
//    val capacity: Int,
//    val location: String
//)