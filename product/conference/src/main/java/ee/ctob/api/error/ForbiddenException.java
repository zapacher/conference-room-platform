package ee.ctob.api.error;


import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {
   private final ErrorResponse error;

   public ForbiddenException(String message) {
      super(message);
      error = new ErrorResponse(403, getMessage());
   }
}
