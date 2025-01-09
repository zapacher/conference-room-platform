package testUtils

import ee.ctob.access.data.Conference
import ee.ctob.api.Request
import ee.ctob.api.enums.Gender
import java.time.LocalDateTime
import java.util.*

class ObjectCreators {

    fun createRegitrationRequest(coonferenceUUID: UUID): Request {
        return Request(
            firstName = "Chuck",
            lastName = "Norris",
            gender = Gender.MALE,
            email = "chuck.norris@hot.me",
            dateOfBirth = LocalDateTime.parse("1940-04-10T00:00:00"),
            conferenceUUID = coonferenceUUID
        )
    }

    fun createRegitrationCancelRequest(validationUUID: UUID): Request {
        return Request(
            validationUUID = validationUUID
        )
    }

    fun createFeedbackRequest(validationUUID: UUID, feedbackText: String): Request {
        return Request(
            validationUUID = validationUUID,
            feedback = feedbackText
        )
    }

    fun createRequestForConfernces(from: String, until: String): Request {
        return Request(
            from = LocalDateTime.parse(from),
            until = LocalDateTime.parse(until)
        )
    }

    fun getConferenceList(): List<Conference> {
        val conference1 = Conference(
            conferenceUUID = UUID.randomUUID(),
            roomUUID = UUID.randomUUID(),
            info = "Some info conference1",
            bookedFrom = LocalDateTime.parse("2024-12-10T16:00:00"),
            bookedUntil = LocalDateTime.parse("2024-12-10T20:00:00"),
            participants = listOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        )

        val conference2 = Conference(
            conferenceUUID = UUID.randomUUID(),
            roomUUID = UUID.randomUUID(),
            info = "Some info conference2",
            bookedFrom = LocalDateTime.parse("2024-12-11T10:00:00"),
            bookedUntil = LocalDateTime.parse("2024-12-11T12:00:00")
        )

        val conference3 = Conference(
            conferenceUUID = UUID.randomUUID(),
            roomUUID = UUID.randomUUID(),
            participants = listOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
            info = "Some info conference3",
            bookedFrom = LocalDateTime.parse("2024-12-18T16:00:00"),
            bookedUntil = LocalDateTime.parse("2024-12-18T16:20:00")
        )

        val conference4 = Conference(
            conferenceUUID = UUID.randomUUID(),
            roomUUID = UUID.randomUUID(),
            participants = listOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
            info = "Some info conference4",
            bookedFrom = LocalDateTime.parse("2024-12-23T20:00:00"),
            bookedUntil = LocalDateTime.parse("2024-12-24T20:00:00")
        )

        val conference5 = Conference(
            conferenceUUID = UUID.randomUUID(),
            roomUUID = UUID.randomUUID(),
            participants = emptyList<UUID>(),
            info = "Some info conference5",
            bookedFrom = LocalDateTime.parse("2024-12-30T18:00:00"),
            bookedUntil = LocalDateTime.parse("2024-12-30T22:00:00")
        )

        return listOf(conference1, conference2, conference3, conference4, conference5)
    }
}