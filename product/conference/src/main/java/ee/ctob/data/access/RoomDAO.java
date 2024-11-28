//package ee.ctob.data.access;
//
//import ee.ctob.data.Room;
//import ee.ctob.data.enums.RoomStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
//@Repository
//public interface RoomDAO extends JpaRepository<Room, Integer> {
//    @Transactional
//    @Query(
//            value = "SELECT location FROM rooms " +
//                    "WHERE room_uuid = ?1",
//            nativeQuery = true
//    )
//    String getRoomLocationByRoomId(UUID roomUUID);
//}
