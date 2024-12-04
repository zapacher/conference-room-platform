package ee.ctob.access;

import ee.ctob.data.Participant;
import ee.ctob.data.access.BaseParticipantDAO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantDAO extends BaseParticipantDAO {

    @Query(
            value = "SELECT * FROM conference.participants " +
                    "WHERE validation_uuid = ?1",
            nativeQuery = true
    )
    Optional<Participant> getParticipant(UUID validationUUID);

    @Modifying
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
