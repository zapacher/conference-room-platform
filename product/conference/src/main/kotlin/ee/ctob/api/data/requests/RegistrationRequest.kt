package ee.ctob.api.data.requests

import com.fasterxml.jackson.annotation.JsonFormat
import ee.ctob.data.enums.Gender
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.Past

data class RegistrationRequest(
    @Schema(example = "Chuck")
    val firstName: String,

    @Schema(example = "Norris")
    val lastName: String,

    @Schema(example = "MALE")
    val gender: Gender,

    @Schema(example = "1940-04-10")
    @Past
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dateOfBirth: LocalDate,

    @Schema(example = "chuck.norris@gmail.com")
    @Email
    val email: String,

    @Schema(example = "af7c1fe6-d669-414e-b066-e9733f0de7a8")
    val conferenceUUID: UUID,
)
