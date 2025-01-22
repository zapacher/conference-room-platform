package ee.ctob.api.data.requests

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

data class FeedbackRequest(
    @Schema(example = "08c71152-c552-42e7-b094-f510ff44e9cb")
    val validationUUID: UUID,

    @Schema(example = "Simple text")
    val feedback: String
) : Request()
