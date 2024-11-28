package ee.ctob.api;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ConferenceAvailable {
    UUID conferenceUUID;
    String location;
    Integer participantsAmount;
}
