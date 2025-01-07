package ee.ctob.api.error

class PreconditionsFailedException(
    message: String
) : RuntimeException(message) {

    val error: ErrorResponse = ErrorResponse(100, message)
}
