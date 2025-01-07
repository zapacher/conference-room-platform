package ee.ctob.data.access;

import ee.ctob.data.Conference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseConferenceDAO extends JpaRepository<Conference, Integer> {

}
