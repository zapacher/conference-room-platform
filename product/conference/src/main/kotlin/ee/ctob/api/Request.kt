package ee.ctob.api

import ee.ctob.api.enums.Gender
import ee.ctob.api.groups.ConferenceAvailable
import ee.ctob.api.groups.Feedback
import ee.ctob.api.groups.Registration
import ee.ctob.api.groups.RegistrationCancel
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Past
import java.time.LocalDateTime
import java.util.UUID

data class Request(
    @field:NotNull(groups = [ConferenceAvailable::class])
    @field:Schema(example = "2024-12-28T11:00:00")
    val from: LocalDateTime,

    @field:Schema(example = "2024-12-28T12:00:00")
    @field:NotNull(groups = [ConferenceAvailable::class])
    val until: LocalDateTime,

    @field:Schema(example = "Chuck")
    @field:NotEmpty(groups = [Registration::class])
    val firstName: String,

    @field:Schema(example = "Norris")
    @field:NotEmpty(groups = [Registration::class])
    val lastName: String,

    @field:Schema(example = "MALE")
    @field:NotNull(groups = [Registration::class])
    val gender: Gender,

    @field:Schema(example = "chuck.norris@gmail.com")
    @field:NotNull(groups = [Registration::class])
    @field:Email
    val email: String,

    @field:Schema(example = "1940-04-10T0:00:00")
    @field:Past(groups = [Registration::class])
    @field:NotNull(groups = [Registration::class])
    val dateOfBirth: LocalDateTime,

    @field:Schema(example = "af7c1fe6-d669-414e-b066-e9733f0de7a8")
    @field:NotNull(groups = [Registration::class])
    val conferenceUUID: UUID,

    @field:Schema(example = "08c71152-c552-42e7-b094-f510ff44e9cb")
    @field:NotNull(groups = [RegistrationCancel::class, Feedback::class])
    val validationUUID: UUID,

    @field:Schema(example = "Simple text")
    @field:NotEmpty(groups = [Feedback::class])
    val feedback: String
)