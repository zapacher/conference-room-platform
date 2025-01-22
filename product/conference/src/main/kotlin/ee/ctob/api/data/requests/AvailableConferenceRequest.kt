package ee.ctob.api.data.requests

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class AvailableConferenceRequest(
    @Schema(example = "2024-12-28T11:00:00")
    val from: LocalDateTime,

    @Schema(example = "2024-12-28T12:00:00")
    val until: LocalDateTime
) : Request()
