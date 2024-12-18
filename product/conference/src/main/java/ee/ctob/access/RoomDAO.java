package ee.ctob.access;

import ee.ctob.data.access.BaseRoomDAO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomDAO extends BaseRoomDAO {
    @Query(
            value = "SELECT location FROM backoffice.rooms " +
                    "WHERE room_uuid = ?1",
            nativeQuery = true
    )
    String getRoomLocationByRoomId(UUID roomUUID);
}
