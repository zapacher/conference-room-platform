package ee.ctob.access


import ee.ctob.data.Participant
import ee.ctob.data.access.BaseParticipantDAO
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
@Repository
interface ParticipantDAO : BaseParticipantDAO {

    @Query(
        value = "SELECT * FROM conference.participants " +
                "WHERE validation_uuid = ?1",
        nativeQuery = true
    )
    fun getParticipant(validationUUID: UUID): Participant?

    @Modifying
    @Query(
        value = "UPDATE conference.participants cp " +
                "SET cp.feedback = ?2 " +
                "WHERE cp.validation_uuid = ?1 " +
                " AND cp.feedback IS NULL " +
                " AND EXISTS ( " +
                "     SELECT 1 " +
                "     FROM backoffice.conferences c " +
                "     JOIN backoffice.conference_participants cpar " +
                "       ON c.id = cpar.conference_id " +
                "     WHERE cpar.participant_uuid = cp.participant_uuid " +
                "     AND c.booked_until < NOW() " +
                " )",
        nativeQuery = true
    )
    fun feedback(validationUUID: UUID, feedback: String): Int
}
