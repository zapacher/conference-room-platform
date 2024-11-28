package ee.ctob.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    UUID validationUUID;
    boolean feedbackResult;
    boolean registrationCancel;
    List<ConferenceAvailable> conferenceAvailableList;
    String reason;
}
