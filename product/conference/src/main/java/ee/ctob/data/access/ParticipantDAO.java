package ee.ctob.data.access;

import ee.ctob.data.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface ParticipantDAO extends JpaRepository<Participant, Integer> {
    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM participants " +
                    "WHERE validation_uuid = ?1 ",
            nativeQuery = true
    )
    int deleteByValidationUUID(UUID validationUUID);

    @Modifying
    @Transactional
    @Query(
            value = "UPDATE participants " +
                    "SET feedback = ?2 " +
                    "WHERE validation_uuid = ?1 " +
                    "AND feedback IS NULL ",
            nativeQuery = true
    )
    int feedback(UUID validationUUID, String feedback);
}
