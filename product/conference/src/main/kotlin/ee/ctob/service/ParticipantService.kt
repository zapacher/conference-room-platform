package ee.ctob.service

import ee.ctob.access.ConferenceDAO
import ee.ctob.access.ParticipantDAO
import ee.ctob.access.RoomDAO
import ee.ctob.api.dto.RequestDTO
import ee.ctob.api.dto.ResponseDTO
import ee.ctob.api.error.BadRequestException
import ee.ctob.api.error.PreconditionsFailedException
import ee.ctob.data.Participant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime.now
import java.util.*

@Service
@Transactional
open class ParticipantService (
    @Autowired private val participantDAO: ParticipantDAO,
    val conferenceDAO: ConferenceDAO,
    private val roomDAO: RoomDAO
) {

    fun registration(requestDTO: RequestDTO): ResponseDTO {
        val newParticipantUUID = UUID.randomUUID()
        if (conferenceDAO.registerParticipant(newParticipantUUID, requestDTO.conferenceUUID!!) == 0) {
            throw PreconditionsFailedException("Registration isn't available for this conference")
        }

        val participant = participantDAO.saveAndFlush(
            Participant().apply {
                created = now()
                validationUUID = UUID.randomUUID()
                participantUUID = newParticipantUUID
                firstName = requestDTO.firstName
                lastName = requestDTO.lastName
                email = requestDTO.email
                gender = requestDTO.gender
                dateOfBirth = requestDTO.dateOfBirth
            }
        )

        return ResponseDTO(
            validationUUID = participant.validationUUID
        )
    }

    fun registrationCancel(requestDTO: RequestDTO): ResponseDTO {
        val participant = participantDAO.getParticipant(requestDTO.validationUUID!!) ?:
        throw PreconditionsFailedException("Participant with this validation doesn't exists")

        val participantUUID = participant.participantUUID

        conferenceDAO.isAvailableForCancel(participantUUID!!) ?:
        throw PreconditionsFailedException("Conference already started or finished")

        if (conferenceDAO.cancelRegistration(participantUUID) == 0) {
            throw PreconditionsFailedException("Validation uuid isn't valid")
        }

        return ResponseDTO(
            registrationCancel = true
        )
    }

    fun feedback(requestDTO: RequestDTO): ResponseDTO {
        if (participantDAO.feedback(requestDTO.validationUUID!!, requestDTO.feedback!!) == 0) {
            throw PreconditionsFailedException("Feedback already exists or conference isn't finished")
        }

        return ResponseDTO(
            validationUUID = requestDTO.validationUUID,
            feedbackResult = true
        )
    }

    fun availableConferences(requestDTO: RequestDTO): ResponseDTO {
        if (now().isAfter(requestDTO.from) || requestDTO.until!!.isBefore(requestDTO.from)) {
            throw BadRequestException(400, "Requested time isn't logical")
        }

        val conferenceList = conferenceDAO.findAllAvailableBetween(requestDTO.from!!, requestDTO.until!!)

        if(conferenceList.isEmpty()) {
            throw PreconditionsFailedException("No conferences are available at this time period")
        }

        val conferenceAvailableList = conferenceList.map { conference ->
            ResponseDTO.ConferenceAvailableDTO(
                conferenceUUID = conference.conferenceUUID,
                location = roomDAO.getRoomLocationByRoomId(conference.roomUUID!!),
                participantsAmount = conference.participants?.size,
                info = conference.info,
                from = conference.bookedFrom,
                until = conference.bookedUntil
            )
        }

        return ResponseDTO(
            conferenceAvailableList = conferenceAvailableList
        )
    }
}
