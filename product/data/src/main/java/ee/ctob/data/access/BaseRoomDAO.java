package ee.ctob.data.access;

import ee.ctob.data.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseRoomDAO extends JpaRepository<Room, Integer> {
}
