package ee.ctob.data.access;

import ee.ctob.data.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantDAO extends JpaRepository<Participant, Integer> {
//    @Transactional
//    @Query(
//            value = "SELECT * FROM conference.participants " +
//                    "WHERE conference_uuid = ?1 " +
//                    "AND feedback IS NOT NULL ",
//            nativeQuery = true
//    )
//    List<Participant> findAllByConferenceUUID(UUID confereneUUID);

    @Transactional
    @Query(
            value = "SELECT * FROM conference.participants " +
                    "WHERE participant_uuid = ?1 " +
                    "AND feedback IS NOT NULL ",
            nativeQuery = true
    )
    Participant findByParticipantUUID(UUID participantUUID);
}