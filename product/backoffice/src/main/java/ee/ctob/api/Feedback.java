package ee.ctob.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Feedback {
    String shortName;
    String feedback;
}
