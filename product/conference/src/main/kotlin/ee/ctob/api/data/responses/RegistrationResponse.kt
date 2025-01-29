package ee.ctob.api.data.responses

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

data class RegistrationResponse(
    @Schema(example = "2d790a4d-7c9c-4e23-9c9c-5749c5fa7fdb")
    var validationUUID: UUID? = null
)
