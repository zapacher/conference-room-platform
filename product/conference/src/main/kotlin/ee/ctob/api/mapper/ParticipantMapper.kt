package ee.ctob.api.mapper

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
import org.mapstruct.factory.Mappers

@Mapper(componentModel = "spring")
interface ParticipantMapper {
    companion object {
        val INSTANCE: ParticipantMapper = Mappers.getMapper(ParticipantMapper::class.java)
    }

    fun toDTO(request: RegistrationRequest): RequestDTO
    fun toDTO(request: RegistrationCancelRequest): RequestDTO
    fun toDTO(request: AvailableConferenceRequest): RequestDTO
    fun toDTO(request: FeedbackRequest): RequestDTO

    fun toRegistrationResponse(responseDTO: ResponseDTO): RegistrationResponse
    fun toCancelResponse(responseDTO: ResponseDTO): RegistrationCancelResponse
    fun toFeedbackResponse(responseDTO: ResponseDTO): FeedbackResponse
    fun toConferenceResponse(responseDTO: ResponseDTO): AvailableConferenceListResponse

}