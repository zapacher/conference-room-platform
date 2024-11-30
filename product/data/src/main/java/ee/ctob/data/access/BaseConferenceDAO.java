package ee.ctob.data.access;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseConferenceDAO<T, ID extends Serializable> extends JpaRepository<T, ID> {

}
