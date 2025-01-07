package ee.ctob.api.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;


@Data
@Setter(AccessLevel.PRIVATE)
public class ErrorResponse {
   private final int code;
   private final String message;

   @JsonCreator
   public ErrorResponse(
           @JsonProperty("code") int code,
           @JsonProperty("message") String message) {
      this.code = code;
      this.message = message;
   }
}
