package ee.ctob.api.dto

import ee.ctob.data.enums.Gender
import java.time.LocalDateTime
import java.util.*

data class RequestDTO(
    var from: LocalDateTime? = null,

    var until: LocalDateTime? = null,

    var firstName: String? = null,

    var lastName: String? = null,

    var gender: Gender? = null,

    var email: String? = null,

    var dateOfBirth: LocalDateTime? = null,

    var conferenceUUID: UUID? = null,

    var validationUUID: UUID? = null,

    var feedback: String? = null

)
