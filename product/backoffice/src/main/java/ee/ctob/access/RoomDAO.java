package ee.ctob.access;

import ee.ctob.access.data.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomDAO extends JpaRepository<Room, Integer> {

    @Query(
            value = "UPDATE backoffice.rooms " +
                    "SET status = ?2 " +
                    "WHERE validation_uuid = ?1 " +
                    "RETURNING * ",
            nativeQuery = true
    )
    Room updateStatus(UUID validationUUID, String roomStatus);

    @Query(
            value = "UPDATE backoffice.rooms " +
                    "SET capacity = ?2 " +
                    "WHERE validation_uuid = ?1 " +
                    "RETURNING * ",
            nativeQuery = true
    )
    Room updateCapacity(UUID validationUUID, Integer capacity);

    @Query(
            value = "SELECT * FROM backoffice.rooms " +
                    "WHERE validation_uuid = ?1 ",
            nativeQuery = true
    )
    Optional<Room> getRoomByValidationUUID(UUID validationUUID);

    @Query(
            value = "SELECT capacity FROM backoffice.rooms " +
                    "WHERE room_uuid = ?1 ",
            nativeQuery = true
    )
    Integer getRoomCapacityByRoomId(UUID roomUUID);

    @Query(
            value = "SELECT * FROM backoffice.rooms " +
                    "WHERE room_uuid = ?1 " +
                    "AND status = 'AVAILABLE' ",
            nativeQuery = true
    )
    Optional<Room> isRoomAvailable(UUID roomUUID);
}
