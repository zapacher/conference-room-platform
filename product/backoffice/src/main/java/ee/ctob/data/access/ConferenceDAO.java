package ee.ctob.data.access;

import ee.ctob.data.Conference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConferenceDAO extends JpaRepository<Conference, Integer> {

    @Modifying
    @Transactional
    @Query(
            value = "UPDATE backoffice.conferences " +
                    "SET status = 'CLOSED' " +
                    "WHERE room_uuid = ( " +
                    "   SELECT room_uuid " +
                    "   FROM backoffice.rooms " +
                    "   WHERE validation_uuid = ?1 " +
                    ") " +
                    "AND booked_from > NOW() " +
                    "AND NOW() NOT BETWEEN booked_from AND booked_until " +
                    "AND ( " +
                    "    SELECT COUNT(*) FROM backoffice.conference_participants " +
                    "    WHERE conference_id = conferences.id " +
                    ") > ?2",
            nativeQuery = true
    )
    void closeConferenceOverlappingCountByRoomUUID(UUID roomUUID, int count);


    @Transactional
    @Query(
            value = "SELECT * FROM backoffice.conferences " +
                    "WHERE validation_uuid = ?1 " +
                    "AND status = 'AVAILABLE' ",
            nativeQuery = true
    )
    Conference getConferenceByValidationUUID(UUID validationUUID);


    @Transactional
    @Query(
            value = "SELECT COUNT(*) FROM backoffice.conferences " +
                    "WHERE ?2 < booked_until AND ?3 > booked_from " +
                    "AND room_uuid = ?1 ",
            nativeQuery = true
    )
    int countOverlappingBookings(UUID roomUUID, LocalDateTime from,LocalDateTime until);

    @Modifying
    @Transactional
    @Query(
            value = "WITH closed_conferences AS (" +
                    "   UPDATE backoffice.conferences " +
                    "   SET status = 'CANCELED' " +
                    "   WHERE room_uuid = ?1 " +
                    "   AND booked_from > NOW() " +
                    "   AND NOW() NOT BETWEEN booked_from AND booked_until " +
                    "   AND status != 'CANCELED' " +
                    "   RETURNING id" +
                    ") " +
                    "DELETE FROM backoffice.conference_participants " +
                    "WHERE conference_id IN (SELECT id FROM closed_conferences)",
            nativeQuery = true
    )
    void closeConferencesByRoomUUID(UUID roomUUID);

    @Modifying
    @Transactional
    @Query(
            value = "WITH canceled_conference AS (" +
                    "   UPDATE backoffice.conferences " +
                    "   SET status = 'CANCELED' " +
                    "   WHERE validation_uuid = ?1 " +
                    "   AND status != 'CANCELED' " +
                    "   RETURNING id" +
                    ") " +
                    "DELETE FROM backoffice.conference_participants " +
                    "WHERE conference_id IN (SELECT id FROM canceled_conference)",
            nativeQuery = true
    )
    int cancelConference(UUID validationUUID);


    @Transactional
    @Query(
            value = "SELECT participant_uuid FROM backoffice.conference_participants " +
                    "WHERE conference_id = ( " +
                    "   SELECT id FROM backoffice.conferences " +
                    "   WHERE validation_uuid = ?1" +
                    ")",
            nativeQuery = true
    )
    List<UUID> getParticipantsUUIDByValidationUUID(UUID validationUUID);
}
