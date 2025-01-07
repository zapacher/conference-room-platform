package ee.ctob.data.access;

import ee.ctob.data.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseParticipantDAO extends JpaRepository<Participant, Integer> {
}