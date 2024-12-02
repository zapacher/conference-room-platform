package ee.ctob.access;

import ee.ctob.data.Participant;
import ee.ctob.data.access.BaseParticipantDAO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface ParticipantDAO extends BaseParticipantDAO<Participant, Integer> {

    @Transactional
    @Query(
            value = "SELECT * FROM conference.participants " +
                    "WHERE validation_uuid = ?1",
            nativeQuery = true
    )
    Participant getParticipantUUID(UUID validationUUID);

    @Modifying
    @Transactional
    @Query(
            value = "UPDATE conference.participants " +
                    "SET feedback = ?2 " +
                    "WHERE validation_uuid = ?1 " +
                    "AND feedback IS NULL " +
                    "AND NOW() > ( " +
                    "   SELECT booked_until FROM backoffice.conferences " +
                    "   WHERE id = ( " +
                    "       SELECT conference_id FROM backoffice.conference_participants " +
                    "       WHERE participant_uuid = ( " +
                    "           SELECT participant_uuid FROM conference.participants " +
                    "           WHERE validation_uuid = ?1 " +
                    "       ) " +
                    "   ) " +
                    ") ",
            nativeQuery = true
    )
    int feedback(UUID validationUUID, String feedback);
}
