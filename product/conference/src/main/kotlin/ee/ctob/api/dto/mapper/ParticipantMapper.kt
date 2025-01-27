package ee.ctob.api.dto.mapper

import ee.ctob.api.data.requests.AvailableConferenceRequest
import ee.ctob.api.data.requests.FeedbackRequest
import ee.ctob.api.data.requests.RegistrationCancelRequest
import ee.ctob.api.data.requests.RegistrationRequest
import ee.ctob.api.data.responses.AvailableConferenceListResponse
import ee.ctob.api.data.responses.FeedbackResponse
import ee.ctob.api.data.responses.RegistrationCancelResponse
import ee.ctob.api.data.responses.RegistrationResponse
import ee.ctob.api.dto.RequestDTO
import ee.ctob.api.dto.ResponseDTO
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface ParticipantMapper {

    fun toDTO(request: RegistrationRequest): RequestDTO
    fun toDTO(request: RegistrationCancelRequest): RequestDTO
    fun toDTO(request: AvailableConferenceRequest): RequestDTO
    fun toDTO(request: FeedbackRequest): RequestDTO

    fun toRegistrationResponse(responseDTO: ResponseDTO): RegistrationResponse
    fun toCancelResponse(responseDTO: ResponseDTO): RegistrationCancelResponse
    fun toFeedbackResponse(responseDTO: ResponseDTO): FeedbackResponse

    @Mapping(source = "responseDTO.conferenceAvailableList", target = "conferenceAvailableList")
    fun toConferenceResponse(responseDTO: ResponseDTO): AvailableConferenceListResponse
}