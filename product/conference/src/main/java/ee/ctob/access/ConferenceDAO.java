package ee.ctob.access;

import ee.ctob.data.Conference;
import ee.ctob.data.access.BaseConferenceDAO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConferenceDAO extends BaseConferenceDAO<Conference, Integer> {

    @Transactional
    @Query(
            value = "SELECT conference_uuid FROM backoffice.conferences " +
                    "WHERE NOW() < booked_from " +
                    "AND conference_id = ( " +
                    "   SELECT conference_id FROM backoffice.conference_participants " +
                    "   WHERE participant_uuid = ?1 " +
                    ") ",
            nativeQuery = true
    )
    UUID isAvailableForCancel(UUID participantUUID);

    @Modifying
    @Transactional
    @Query(
            value = "WITH conference AS ( " +
                    "    SELECT c.id AS conference_id, c.room_uuid, c.booked_from " +
                    "    FROM backoffice.conferences c " +
                    "    WHERE c.conference_uuid = ?2 " +
                    "), " +
                    "room AS ( " +
                    "    SELECT r.room_uuid, r.capacity, r.status " +
                    "    FROM backoffice.rooms r " +
                    "    WHERE r.room_uuid = (SELECT room_uuid FROM conference) " +
                    ") " +
                    "INSERT INTO backoffice.conference_participants (conference_id, participant_uuid) " +
                    "SELECT (SELECT conference_id FROM conference) , ?1 " +
                    "WHERE ( " +
                    "    SELECT COUNT(*) " +
                    "    FROM backoffice.conference_participants cp " +
                    "    WHERE cp.conference_id = (SELECT conference_id FROM conference) " +
                    ") < (SELECT capacity FROM room) " +
                    "AND NOW() < (SELECT booked_from FROM conference) " +
                    "AND 'AVAILABLE' = (SELECT status FROM room)",
            nativeQuery = true
    )
    int registerParticipant(UUID participantUUID, UUID conferenceUUID);

    @Transactional
    @Query(
            value = "SELECT * FROM backoffice.conferences " +
                    "WHERE booked_from > ?1 " +
                    "AND booked_until < ?2 " +
                    "AND status = 'AVAILABLE' ",
            nativeQuery = true
    )
    List<Conference> findAllAvailableBetween(LocalDateTime from, LocalDateTime until);

    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM backoffice.conference_participants  " +
                    "WHERE participant_uuid = ?1 ",
            nativeQuery = true
    )
    int cancelRegistration(UUID participantUUID);
}
