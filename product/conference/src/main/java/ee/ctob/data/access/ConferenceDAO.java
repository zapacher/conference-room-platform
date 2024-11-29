package ee.ctob.data.access;

import ee.ctob.data.Conference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface ConferenceDAO extends JpaRepository<Conference, Integer> {

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

//    @Modifying
//    @Transactional
//    @Query(
//            value = "INSERT INTO backoffice.conference_participants (conference_id, participant_uuid) " +
//                    "VALUES (?1, ?2) ",
//            nativeQuery = true
//    )
//    void addParticipant(Integer conferenceId, UUID participantUUID);
//    @Modifying
//    @Transactional
//    @Query(
//            value = "DELETE FROM conference_participants " +
//                    "WHERE conference_id = ?1 " +
//                    "AND participant_uuid = ?2 ",
//            nativeQuery = true
//    )
//    void removeParticipant(Integer conferenceId, UUID participantUUID);
}
