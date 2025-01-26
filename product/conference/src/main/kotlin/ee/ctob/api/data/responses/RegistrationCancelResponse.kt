package ee.ctob.api.data.responses

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

data class RegistrationCancelResponse(
    @Schema(example = "08c71152-c552-42e7-b094-f510ff44e9cb")
    var validationUUID: UUID? = null,
    @Schema(example = "true")
    var registrationCancel: Boolean = false
)
