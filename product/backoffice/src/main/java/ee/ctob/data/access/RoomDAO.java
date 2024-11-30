package ee.ctob.data.access;

import ee.ctob.data.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface RoomDAO extends JpaRepository<Room, Integer> {

    @Transactional
    @Query(
            value = "UPDATE backoffice.rooms " +
                    "SET status = ?2 " +
                    "WHERE validation_uuid = ?1 " +
                    "RETURNING * ",
            nativeQuery = true
    )
    Room updateStatus(UUID validationUUID, String roomStatus);

    @Transactional
    @Query(
            value = "UPDATE backoffice.rooms " +
                    "SET capacity = ?2 " +
                    "WHERE validation_uuid = ?1 " +
                    "RETURNING * ",
            nativeQuery = true
    )
    Room updateCapacity(UUID validationUUID, Integer capacity);

    @Transactional
    @Query(
            value = "SELECT capacity FROM backoffice.rooms " +
                    "WHERE validation_uuid = ?1 ",
            nativeQuery = true
    )
    Integer getRoomCapacity(UUID validationUUID);

    @Transactional
    @Query(
            value = "SELECT * FROM backoffice.rooms " +
                    "WHERE validation_uuid = ?1 ",
            nativeQuery = true
    )
    Room getRoomByValidationUUID(UUID validationUUID);

    @Transactional
    @Query(
            value = "SELECT capacity FROM backoffice.rooms " +
                    "WHERE room_uuid = ?1 ",
            nativeQuery = true
    )
    Integer getRoomCapacityByRoomId(UUID roomUUID);

    @Transactional
    @Query(
            value = "SELECT COUNT(*) FROM backoffice.rooms " +
                    "WHERE room_uuid = ?1 " +
                    "AND status = 'AVAILABLE' ",
            nativeQuery = true
    )
    int isRoomAvailable(UUID roomUUID);
}
