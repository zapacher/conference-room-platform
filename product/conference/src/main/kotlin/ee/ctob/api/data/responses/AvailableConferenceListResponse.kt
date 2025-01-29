package ee.ctob.api.data.responses

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

data class AvailableConferenceListResponse(
    @Schema(example = "List of schema ConferenceAvailable")
    var conferenceAvailableList: List<ConferenceAvailable>? = null
) {
    data class ConferenceAvailable(
        @Schema(example = "5a743569-35f8-4588-899a-7ebcd4a75def")
        val conferenceUUID: UUID,

        @Schema(example = "Tartu mnt. 62, floor 30, room 247")
        val location: String,

        @Schema(example = "20")
        val participantsAmount: Int,

        @Schema(example = "2024-12-30T12:00:00")
        val from: LocalDateTime,

        @Schema(example = "2024-12-30T17:30:00")
        val until: LocalDateTime,

        @Schema(example = "Info about conference")
        var info: String? = null
    )
}
