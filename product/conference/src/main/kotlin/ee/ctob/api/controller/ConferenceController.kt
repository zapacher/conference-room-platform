package ee.ctob.api.controller

import ee.ctob.api.data.requests.AvailableConferenceRequest
import ee.ctob.api.data.requests.FeedbackRequest
import ee.ctob.api.data.requests.RegistrationCancelRequest
import ee.ctob.api.data.requests.RegistrationRequest
import ee.ctob.api.data.responses.AvailableConferenceListResponse
import ee.ctob.api.data.responses.FeedbackResponse
import ee.ctob.api.data.responses.RegistrationCancelResponse
import ee.ctob.api.data.responses.RegistrationResponse
import ee.ctob.api.dto.mapper.ParticipantMapper
import ee.ctob.service.ParticipantService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.mapstruct.factory.Mappers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController("conference")
@RequestMapping(
    path = ["/conference"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
    consumes = [MediaType.APPLICATION_JSON_VALUE]
)
class ConferenceController(
    private var participantService: ParticipantService,
    private var participantMapper: ParticipantMapper = Mappers.getMapper(ParticipantMapper::class.java)
) {

    @Operation(summary = "Register new participant to conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "firstName, lastName, gender, email, dateOfBirth, conferenceUUID")
    @ApiResponses(value = [
        ApiResponse(responseCode = "100", description = "Any precondition errors will be explained"),
        ApiResponse(responseCode = "200", description = "validationUUID will be in response if success"),
        ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")])
    @PostMapping("/registration/create")
    fun registration(@RequestBody request: RegistrationRequest): RegistrationResponse {
        println("we are IN")
        val responseDTO = participantService.registration(participantMapper.toDTO(request))
        return participantMapper.toRegistrationResponse(responseDTO)
    }

    @Operation(summary = "Cancel registration")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID")
    @ApiResponses(value = [
        ApiResponse(responseCode = "100", description = "Any precondition errors will be explained"),
        ApiResponse(responseCode = "200", description = "registrationCancel = true will be in response if success"),
        ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")])
    @PostMapping("/registration/cancel")
    fun registrationCancel(@RequestBody request: RegistrationCancelRequest): RegistrationCancelResponse {
        val responseDTO = participantService.registrationCancel(participantMapper.toDTO(request))
        return participantMapper.toCancelResponse(responseDTO)
    }

    @Operation(summary = "Leave feedback after conference")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "validationUUID, feedback")
    @ApiResponses(value = [
        ApiResponse(responseCode = "100", description = "Any precondition errors will be explained"),
        ApiResponse(responseCode = "200", description = "feedbackResult = true will be in response if success"),
        ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")])
    @PostMapping("/feedback/create")
    fun feedback(@RequestBody request: FeedbackRequest): FeedbackResponse {
        val responseDTO = participantService.feedback(participantMapper.toDTO(request))
        return participantMapper.toFeedbackResponse(responseDTO)
    }

    @Operation(summary = "Get available conferences")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "from, until")
    @ApiResponses(value = [
        ApiResponse(responseCode = "100", description = "Any precondition errors will be explained"),
        ApiResponse(responseCode = "200", description = "List of schema ConferenceAvailable response if success"),
        ApiResponse(responseCode = "400", description = "If required values will be null/empty/format")])
    @PostMapping("/available")
    fun availableConferences(@RequestBody request: AvailableConferenceRequest): AvailableConferenceListResponse {
        val responseDTO = participantService.availableConferences(participantMapper.toDTO(request))
        return participantMapper.toConferenceResponse(responseDTO)
    }
}