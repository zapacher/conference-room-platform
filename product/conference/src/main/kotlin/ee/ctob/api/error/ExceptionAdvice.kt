package ee.ctob.api.error

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.slf4j.LoggerFactory

@ControllerAdvice
class ExceptionAdvice {

    private val log = LoggerFactory.getLogger(ExceptionAdvice::class.java)

    @ExceptionHandler(BadRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun processException(ex: BadRequestException): ErrorResponse {
        log.warn(ex.message, ex)
        return ex.error
    }

    @ExceptionHandler(PreconditionsFailedException::class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    fun processException(ex: PreconditionsFailedException): ErrorResponse {
        log.warn(ex.message, ex)
        return ex.error
    }
}