package ee.ctob.api.error;


import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
   private final ErrorResponse error;

   public BadRequestException(int errorCode, String message) {
      super(message);
      error = new ErrorResponse(errorCode, message);
   }
}
