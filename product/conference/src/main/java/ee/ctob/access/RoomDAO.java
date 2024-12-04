package ee.ctob.access;

import ee.ctob.data.Room;
import ee.ctob.data.access.BaseRoomDAO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface RoomDAO extends BaseRoomDAO {
    @Transactional
    @Query(
            value = "SELECT location FROM rooms " +
                    "WHERE room_uuid = ?1",
            nativeQuery = true
    )
    String getRoomLocationByRoomId(UUID roomUUID);
}
