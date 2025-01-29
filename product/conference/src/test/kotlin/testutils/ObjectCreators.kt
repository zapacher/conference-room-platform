package testutils

import ee.ctob.api.data.requests.AvailableConferenceRequest
import ee.ctob.api.data.requests.FeedbackRequest
import ee.ctob.api.data.requests.RegistrationCancelRequest
import ee.ctob.api.data.requests.RegistrationRequest
import ee.ctob.data.Conference
import ee.ctob.data.enums.Gender
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class ObjectCreators {
    companion object {
        fun createRegistrationRequest(conferenceUUID: UUID?): RegistrationRequest {
            return RegistrationRequest(
                firstName = "Chuck",
                lastName = "Norris",
                gender = Gender.MALE,
                email = "chuck.norris@hot.me",
                dateOfBirth = LocalDate.of(1940 ,4,10),
                conferenceUUID = conferenceUUID!!
            )
        }

        fun createRegistrationCancelRequest(validationUUID: UUID?): RegistrationCancelRequest {
            return RegistrationCancelRequest(
                validationUUID = validationUUID!!
            )
        }

        fun createFeedbackRequest(validationUUID: UUID?, feedbackText: String?): FeedbackRequest {
            return FeedbackRequest(
                validationUUID = validationUUID!!,
                feedback = feedbackText!!
            )
        }

        fun createRequestForConferences(from: LocalDateTime?, until: LocalDateTime?): AvailableConferenceRequest {
            return AvailableConferenceRequest(
                from = from!!,
                until = until!!
            )
        }

        fun getConferenceList(): List<Conference> {
            val conference1 = Conference().apply {
                conferenceUUID = UUID.randomUUID()
                roomUUID = UUID.randomUUID()
                participants = listOf(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID())
                info = "Some info conference1"
                bookedFrom = LocalDateTime.parse("2024-12-10T16:00:00")
                bookedUntil = LocalDateTime.parse("2024-12-10T20:00:00")
            }

            val conference2 = Conference().apply {
                conferenceUUID = UUID.randomUUID()
                roomUUID = UUID.randomUUID()
                participants = listOf(UUID.randomUUID(), UUID.randomUUID())
                info = "Some info conference2"
                bookedFrom = LocalDateTime.parse("2024-12-11T10:00:00")
                bookedUntil = LocalDateTime.parse("2024-12-11T12:00:00")
            }

            val conference3 = Conference().apply {
                conferenceUUID = UUID.randomUUID()
                roomUUID = UUID.randomUUID()
                participants = listOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                info = "Some info conference3"
                bookedFrom = LocalDateTime.parse("2024-12-18T16:00:00")
                bookedUntil = LocalDateTime.parse("2024-12-18T16:20:00")
            }

            val conference4 = Conference().apply {
                conferenceUUID = UUID.randomUUID()
                roomUUID = UUID.randomUUID()
                participants = listOf(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID())
                info = "Some info conference4"
                bookedFrom = LocalDateTime.parse("2024-12-23T20:00:00")
                bookedUntil = LocalDateTime.parse("2024-12-24T20:00:00")
            }

            val conference5 = Conference().apply {
                conferenceUUID = UUID.randomUUID()
                roomUUID = UUID.randomUUID()
                participants = listOf()
                info = "Some info conference5"
                bookedFrom = LocalDateTime.parse("2024-12-30T18:00:00")
                bookedUntil = LocalDateTime.parse("2024-12-30T22:00:00")
            }

            return listOf(conference1, conference2, conference3, conference4, conference5)
        }
    }
}