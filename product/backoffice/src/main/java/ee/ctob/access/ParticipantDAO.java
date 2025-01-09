package ee.ctob.access;

import ee.ctob.data.Participant;
import ee.ctob.data.access.BaseParticipantDAO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantDAO extends BaseParticipantDAO {

    @Query(
            value = "SELECT * FROM conference.participants " +
                    "WHERE participant_uuid IN ?1 " +
                    "AND feedback IS NOT NULL",
            nativeQuery = true)
    Optional<List<Participant>> findByParticipantUUIDs(List<UUID> participantUUIDs);
}