//package ee.ctob.api
//
//import ee.ctob.api.groups.ConferenceAvailable
//import ee.ctob.api.groups.Feedback
//import ee.ctob.api.groups.Registration
//import ee.ctob.api.groups.RegistrationCancel
//import ee.ctob.data.enums.Gender
//import io.swagger.v3.oas.annotations.media.Schema
//import java.time.LocalDateTime
//import java.util.*
//import javax.validation.constraints.Email
//import javax.validation.constraints.NotEmpty
//import javax.validation.constraints.NotNull
//import javax.validation.constraints.Past
//
//data class Requestw(
//    @NotNull(groups = [ConferenceAvailable::class])
//    @Schema(example = "2024-12-28T11:00:00")
//    var from: LocalDateTime? = null,
//
//    @Schema(example = "2024-12-28T12:00:00")
//    @NotNull(groups = [ConferenceAvailable::class])
//    var until: LocalDateTime? = null,
//
//    @Schema(example = "Chuck")
//    @NotEmpty(groups = [Registration::class])
//    var firstName: String? = null,
//
//    @Schema(example = "Norris")
//    @NotEmpty(groups = [Registration::class])
//    var lastName: String? = null,
//
//    @Schema(example = "MALE")
//    @NotNull(groups = [Registration::class])
//    var gender: Gender? = null,
//
//    @Schema(example = "chuck.norris@gmail.com")
//    @NotNull(groups = [Registration::class])
//    @Email
//    var email: String? = null,
//
//    @Schema(example = "1940-04-10T0:00:00")
//    @Past(groups = [Registration::class])
//    @NotNull(groups = [Registration::class])
//    var dateOfBirth: LocalDateTime? = null,
//
//    @Schema(example = "af7c1fe6-d669-414e-b066-e9733f0de7a8")
//    @NotNull(groups = [Registration::class])
//    var conferenceUUID: UUID? = null,
//
//    @Schema(example = "08c71152-c552-42e7-b094-f510ff44e9cb")
//    @NotNull(groups = [RegistrationCancel::class, Feedback::class])
//    var validationUUID: UUID? = null,
//
//    @Schema(example = "Simple text")
//    @NotEmpty(groups = [Feedback::class])
//    var feedback: String? = null
//)