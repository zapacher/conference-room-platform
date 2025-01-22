//package ee.ctob.api
//
//import com.fasterxml.jackson.annotation.JsonInclude
//import io.swagger.v3.oas.annotations.media.Schema
//import java.time.LocalDateTime
//import java.util.*
//
//@JsonInclude(JsonInclude.Include.NON_NULL)
//data class Response(
//    @Schema(example = "2d790a4d-7c9c-4e23-9c9c-5749c5fa7fdb")
//    var validationUUID: UUID? = null,
//
//    @Schema(example = "true")
//    var feedbackResult: Boolean? = false,
//
//    @Schema(example = "true")
//    var registrationCancel: Boolean? = false,
//
//    @Schema(example = "List of schema ConferenceAvailable")
//    var conferenceAvailableList: List<ConferenceAvailable>? = null
//) {
//
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    data class ConferenceAvailable(
//        @Schema(example = "5a743569-35f8-4588-899a-7ebcd4a75def")
//        var conferenceUUID: UUID? = null,
//
//        @Schema(example = "Tartu mnt. 62, floor 30, room 247")
//        var location: String? = null,
//
//        @Schema(example = "20")
//        var participantsAmount: Int? = null,
//
//        @Schema(example = "2024-12-30T12:00:00")
//        var from: LocalDateTime? = null,
//
//        @Schema(example = "2024-12-30T17:30:00")
//        var until: LocalDateTime? = null,
//
//        @Schema(example = "Info about conference")
//        var info: String? = null
//    )
//}