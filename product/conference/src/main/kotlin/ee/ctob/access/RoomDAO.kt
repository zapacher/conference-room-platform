package ee.ctob.access

import ee.ctob.data.access.BaseRoomDAO
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoomDAO : BaseRoomDAO {

    @Query(
        value = "SELECT location FROM backoffice.rooms " +
                "WHERE room_uuid = ?1",
        nativeQuery = true
    )
    fun getRoomLocationByRoomId(roomUUID: UUID): String
}