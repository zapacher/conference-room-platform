package ee.ctob.api.dto

import java.time.LocalDateTime
import java.util.*

data class ResponseDTO(
    var validationUUID: UUID? = null,
    var feedbackResult: Boolean = false,
    var registrationCancel: Boolean = false,
    var conferenceAvailableList: List<ConferenceAvailableDTO>? = null
) {
    data class ConferenceAvailableDTO(
        var conferenceUUID: UUID? = null,
        var location: String? = null,
        var participantsAmount: Int? = null,
        var from: LocalDateTime? = null,
        var until: LocalDateTime? = null,
        var info: String? = null
    )
}
