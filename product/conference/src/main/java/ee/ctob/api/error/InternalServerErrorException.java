package ee.ctob.api.error;


import lombok.Getter;


@Getter
public class InternalServerErrorException extends RuntimeException {
   private final ErrorResponse error;

   public InternalServerErrorException() {
      super("Unknown error");
      error = new ErrorResponse(500, getMessage());
   }

   public InternalServerErrorException(int errorCode, String message) {
      super(message);
      error = new ErrorResponse(errorCode, message);
   }
}
