package ee.ctob.controller

import ee.ctob.access.ConferenceDAO
import ee.ctob.access.ParticipantDAO
import ee.ctob.access.RoomDAO
import ee.ctob.api.controller.ConferenceController
import ee.ctob.api.data.requests.AvailableConferenceRequest
import ee.ctob.api.data.requests.FeedbackRequest
import ee.ctob.api.data.requests.RegistrationCancelRequest
import ee.ctob.api.data.requests.RegistrationRequest
import ee.ctob.api.data.responses.AvailableConferenceListResponse
import ee.ctob.api.data.responses.FeedbackResponse
import ee.ctob.api.data.responses.RegistrationCancelResponse
import ee.ctob.api.data.responses.RegistrationResponse
import ee.ctob.api.error.BadRequestException
import ee.ctob.api.error.ErrorResponse
import ee.ctob.api.error.PreconditionsFailedException
import ee.ctob.data.Conference
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.ObjectCreators.Companion.createFeedbackRequest
import testutils.ObjectCreators.Companion.createRegistrationCancelRequest
import testutils.ObjectCreators.Companion.createRegistrationRequest
import testutils.ObjectCreators.Companion.createRequestForConferences
import testutils.ObjectCreators.Companion.getConferenceList
import testutils.TestContainer
import java.time.LocalDateTime
import java.util.*

@Testcontainers
@ExtendWith(MockitoExtension::class)
class TestsUnitConferenceController : TestContainer() {
    @MockBean
    lateinit var roomDAO: RoomDAO

    @MockBean
    lateinit var conferenceDAO: ConferenceDAO

    @SpyBean
    lateinit var participantDAO: ParticipantDAO

    @Autowired
    lateinit var controller: ConferenceController

    private var registrationRequest: RegistrationRequest? = null
    private var registrationCancelRequest: RegistrationCancelRequest? = null
    private var feedbackRequest: FeedbackRequest? = null
    private var conferenceAvailableRequest: AvailableConferenceRequest? = null
    private var registrationResponse: RegistrationResponse? = null
    private var registrationCancelResponse: RegistrationCancelResponse? = null
    private var feedbackResponse: FeedbackResponse? = null
    private var availableConferenceListResponse: AvailableConferenceListResponse? = null
    private var errorResponse: ErrorResponse? = null
    private var participantValidationUUID: UUID? = null

    @Test
    fun registration() {
        registrationRequest = createRegistrationRequest(UUID.randomUUID())
        mockRegistration()
        registrationResponse = controller.registration(registrationRequest!!)

        assertAll("Registration Success",
            { assertNotNull(registrationResponse, "Response") },
            { assertNotNull(registrationResponse?.validationUUID, "validationUUID") }
        )

        participantValidationUUID = registrationResponse?.validationUUID
    }

    @Test
    fun registrationFail() {
        registrationRequest = createRegistrationRequest(UUID.randomUUID())
        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.registration(registrationRequest!!) }.error
        assertAll("Registration Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Registration isn't available for this conference", errorResponse?.message, "error message") }
        )
    }

    @Test
    fun registrationCancel() {
        registration()

        mockConferenceForCancel()
        mockParticipantForCancel(1)

        registrationCancelRequest = createRegistrationCancelRequest(participantValidationUUID)
        registrationCancelResponse = controller.registrationCancel(registrationCancelRequest!!)

        assertAll("Registration cancel success",
            { assertNotNull(registrationCancelResponse, "Response") },
            { assertNull(registrationCancelResponse!!.validationUUID, "validationUUID") },
            { assertTrue(registrationCancelResponse!!.registrationCancel, "registrationCancel") }
        )
    }

    @Test
    fun registrationCancelFail() {
        registration()

        registrationCancelRequest = createRegistrationCancelRequest(UUID.randomUUID())

        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.registrationCancel(registrationCancelRequest!!) }.error
        assertAll("Registration cancel Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Participant with this validation doesn't exists", errorResponse?.message, "error message") }
        )

        registrationCancelRequest = createRegistrationCancelRequest(participantValidationUUID)
        mockConferenceForCancelThrow()

        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.registrationCancel(registrationCancelRequest!!) }.error
        assertAll("Registration cancel Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Conference already started or finished", errorResponse?.message, "error message") }
        )


        registrationCancelRequest = createRegistrationCancelRequest(participantValidationUUID)
        mockConferenceForCancelThrow()
        mockParticipantForCancel(0)

        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.registrationCancel(registrationCancelRequest!!) }.error
        assertAll("Registration cancel Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Conference already started or finished", errorResponse?.message, "error message") }
        )
    }

    @Test
    fun feedback() {
        registration()

        feedbackRequest = createFeedbackRequest(participantValidationUUID, "Any text for feedback")
        mockFeedback(1)
        feedbackResponse = controller.feedback(feedbackRequest!!)

        assertAll("Feedback Success",
            { assertNotNull(feedbackResponse, "Response") },
            { assertEquals(participantValidationUUID, feedbackResponse?.validationUUID, "validationUUID") },
            { assertTrue(feedbackResponse?.feedbackResult!!, "feedbackResult") }
        )
    }

    @Test
    fun feedbackFail() {
        registration()

        feedbackRequest = createFeedbackRequest(participantValidationUUID, "Any text for feedback")
        mockFeedback(0)
        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.feedback(feedbackRequest!!) }.error
        assertAll("Feeback Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Feedback already exists or conference isn't finished", errorResponse?.message, "error message")}
        )
    }

    @Test
    fun availableConferences() {
        registration()

        mockConferenceList()
        mockLocation()

        conferenceAvailableRequest = createRequestForConferences(LocalDateTime.now().plusHours(60), LocalDateTime.now().plusHours(65))
        availableConferenceListResponse = controller.availableConferences(conferenceAvailableRequest!!)

        assertAll("Available conferences",
            { assertNotNull(availableConferenceListResponse, "Response") },
            { assertEquals(5, availableConferenceListResponse?.conferenceAvailableList?.size, "conferenceAvailableList") }
        )

        for (conference in availableConferenceListResponse?.conferenceAvailableList!!) {
            assertAll("Conference from availableList",
                { assertNotNull(conference.conferenceUUID, "conferenceUUID") },
                { assertNotNull(conference.participantsAmount, "participantsAmount") },
                { assertNotNull(conference.from, "from") },
                { assertNotNull(conference.until, "until") },
                { assertNotNull(conference.info, "info") },
                { assertEquals("Tallinn", conference.location, "location") }
            )
        }
    }

    @Test
    fun availableConferencesFail() {
        registration()

        conferenceAvailableRequest = createRequestForConferences(LocalDateTime.now().plusHours(40), LocalDateTime.now().plusHours(45))
        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.availableConferences(conferenceAvailableRequest!!) }.error
        assertAll("Available conferences Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("No conferences are available at this time period", errorResponse?.message, "error message") }
        )

        conferenceAvailableRequest = createRequestForConferences(LocalDateTime.now().plusHours(60), LocalDateTime.now().plusHours(59))
        errorResponse = assertThrows(BadRequestException::class.java) { controller.availableConferences(conferenceAvailableRequest!!) }.error
        assertAll("Available conferences Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(400, errorResponse?.code, "error code") },
            { assertEquals("Requested time isn't logical", errorResponse?.message, "error message") }
        )

        conferenceAvailableRequest = createRequestForConferences(LocalDateTime.now().minusHours(60), LocalDateTime.now().plusHours(65))
        errorResponse = assertThrows(BadRequestException::class.java) { controller.availableConferences(conferenceAvailableRequest!!) }.error
        assertAll("Available conferences Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(400, errorResponse?.code, "error code") },
            { assertEquals("Requested time isn't logical", errorResponse?.message, "error message") }
        )
    }

    private fun mockConferenceList() {
        whenever(conferenceDAO.findAllAvailableBetween(any(), any()))
            .thenReturn(getConferenceList())
    }

    private fun mockLocation() {
        whenever(roomDAO.getRoomLocationByRoomId(any())).thenReturn("Tallinn")
    }

    private fun mockFeedback(result: Int) {
        doReturn(result)
            .whenever(participantDAO).feedback(any(), any())
    }

    private fun mockConferenceForCancel() {
        whenever(conferenceDAO.isAvailableForCancel(any()))
            .thenReturn(Conference.builder().conferenceUUID(UUID.randomUUID()).build())
    }

    private fun mockConferenceForCancelThrow() {
        doThrow(PreconditionsFailedException("Conference already started or finished"))
            .whenever(conferenceDAO).isAvailableForCancel(any())
    }

    private fun mockParticipantForCancel(response: Int) {
        whenever(conferenceDAO.cancelRegistration(any())).thenReturn(response)
    }

    private fun mockRegistration() {
        whenever(conferenceDAO.registerParticipant(any(), any()))
            .thenReturn(1)
    }
}