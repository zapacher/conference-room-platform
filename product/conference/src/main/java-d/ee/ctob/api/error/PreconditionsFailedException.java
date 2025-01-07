package ee.ctob.api.error;


import lombok.Getter;

@Getter
public class PreconditionsFailedException extends RuntimeException {
   private final ErrorResponse error;

   public PreconditionsFailedException(String message) {
      super(message);
      error = new ErrorResponse(100, message);
   }
}
