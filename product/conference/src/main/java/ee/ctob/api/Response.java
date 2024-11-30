package ee.ctob.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    @Schema(example = "2d790a4d-7c9c-4e23-9c9c-5749c5fa7fdb")
    UUID validationUUID;
    @Schema(example = "true")
    boolean feedbackResult;
    @Schema(example = "true")
    boolean registrationCancel;
    @Schema(example = "List of schema ConferenceAvailable")
    List<ConferenceAvailable> conferenceAvailableList;
    @Schema(example = "If is error of service, the error is described here")
    String reason;
}
