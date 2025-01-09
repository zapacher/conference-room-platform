package ee.ctob.service

import ee.ctob.access.ConferenceDAO
import ee.ctob.access.ParticipantDAO
import ee.ctob.access.RoomDAO
import ee.ctob.access.data.Participant
import ee.ctob.api.Request
import ee.ctob.api.Response
import ee.ctob.api.error.BadRequestException
import ee.ctob.api.error.PreconditionsFailedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime.now
import java.util.*

@Service
@Transactional
open class ParticipantService (
    private val participantDAO: ParticipantDAO,
    private val conferenceDAO: ConferenceDAO,
    private val roomDAO: RoomDAO
) {

    open fun registration(request: Request): Response {
        val participantUUID = UUID.randomUUID()
        if (conferenceDAO.registerParticipant(participantUUID, request.conferenceUUID) == 0) {
            throw PreconditionsFailedException("Registration isn't available for this conference")
        }

        val participant = participantDAO.saveAndFlush(
            Participant(
                created = now(),
                participantUUID = participantUUID,
                validationUUID = UUID.randomUUID(),
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                gender = request.gender,
                dateOfBirth = request.dateOfBirth
            )
        )

        return Response(
            validationUUID = participant.validationUUID
        )
    }

    open fun registrationCancel(request: Request): Response {
        val participant = participantDAO.getParticipant(request.validationUUID!!)?:
        throw PreconditionsFailedException("Participant with this validation doesn't exists")

        val participantUUID = participant.participantUUID

        conferenceDAO.isAvailableForCancel(participantUUID!!)?:
        throw PreconditionsFailedException("Conference already started or finished")

        if (conferenceDAO.cancelRegistration(participantUUID) == 0) {
            throw PreconditionsFailedException("Validation uuid isn't valid")
        }

        return Response(
            registrationCancel = true
        )
    }

    open fun feedback(request: Request): Response {
        if (participantDAO.feedback(request.validationUUID!!, request.feedback!!) == 0) {
            throw PreconditionsFailedException("Feedback already exists or conference isn't finished")
        }

        return Response(
            validationUUID = request.validationUUID,
            feedbackResult = true
        )
    }

    fun availableConferences(request: Request): Response {
        if (now().isAfter(request.from) || request.until!!.isBefore(request.from)) {
            throw BadRequestException(400, "Requested time isn't logical")
        }

        val conferenceList = conferenceDAO.findAllAvailableBetween(request.from!!, request.until!!)?:
        throw PreconditionsFailedException("No conferences are available at this time period")

        val conferenceAvailableList = conferenceList.map { conference ->
            Response.ConferenceAvailable(
                conferenceUUID = conference.conferenceUUID,
                location = roomDAO.getRoomLocationByRoomId(conference.roomUUID!!),
                participantsAmount = conference.participants?.size,
                info = conference.info,
                from = conference.bookedFrom,
                until = conference.bookedUntil
            )
        }

        return Response(
            conferenceAvailableList = conferenceAvailableList
        )
    }
}
