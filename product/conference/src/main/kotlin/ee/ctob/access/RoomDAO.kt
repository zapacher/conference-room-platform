package ee.ctob.access

import ee.ctob.access.data.Room
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoomDAO : JpaRepository<Room,Int> {

    @Query(
        value = "SELECT location FROM backoffice.rooms " +
                "WHERE room_uuid = ?1",
        nativeQuery = true
    )
    fun getRoomLocationByRoomId(roomUUID: UUID): String
}