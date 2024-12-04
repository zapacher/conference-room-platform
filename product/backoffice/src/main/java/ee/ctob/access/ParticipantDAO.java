package ee.ctob.access;

import ee.ctob.data.Participant;
import ee.ctob.data.access.BaseParticipantDAO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantDAO extends BaseParticipantDAO {
    @Transactional
    @Query(
            value = "SELECT * FROM conference.participants " +
                    "WHERE participant_uuid IN ?1 " +
                    "AND feedback IS NOT NULL",
            nativeQuery = true)
    List<Participant> findByParticipantUUIDs(List<UUID> participantUUIDs);

}