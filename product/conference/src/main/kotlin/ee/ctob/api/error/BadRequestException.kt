package ee.ctob.api.error

class BadRequestException(
    errorCode: Int,
    message: String
) : RuntimeException(message) {

    val error: ErrorResponse = ErrorResponse(errorCode, message)
}