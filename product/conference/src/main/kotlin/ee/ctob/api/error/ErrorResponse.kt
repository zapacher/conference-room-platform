package ee.ctob.api.error

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse @JsonCreator constructor(
    @JsonProperty("code") val code: Int,
    @JsonProperty("message") val message: String
)