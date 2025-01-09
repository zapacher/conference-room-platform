package ee.ctob.controller

import ee.ctob.access.ConferenceDAO
import ee.ctob.access.ParticipantDAO
import ee.ctob.access.RoomDAO
import ee.ctob.access.data.Conference
import ee.ctob.api.Request
import ee.ctob.api.Response
import ee.ctob.api.controller.ConferenceController
import ee.ctob.api.error.BadRequestException
import ee.ctob.api.error.ErrorResponse
import ee.ctob.api.error.PreconditionsFailedException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
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

    @InjectMocks
    @Autowired
    lateinit var controller: ConferenceController

    private var request: Request? = null
    private var response: Response? = null
    private var errorResponse: ErrorResponse? = null
    private var participantValidationUUID: UUID? = null

    @Test
    fun registration() {
        request = createRegistrationRequest(UUID.randomUUID())
        mockRegistration()
        response = controller.registration(request!!)
        assertAll("Registration Success",
            { assertNotNull(response, "Response") },
            { assertNotNull(response?.validationUUID, "validationUUID") }
        )

        participantValidationUUID = response?.validationUUID
    }

    @Test
    fun registrationFail() {
        request = createRegistrationRequest(UUID.randomUUID())
        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.registration(request!!) }.error
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

        request = createRegistrationCancelRequest(participantValidationUUID)
        response = controller.registrationCancel(request!!)

        assertAll("Registration cancel success",
            { assertNotNull(response, "Response") },
            { assertNull(response?.validationUUID, "validationUUID") },
            { assertTrue(response?.registrationCancel!!, "registrationCancel") }
        )
    }

    @Test
    fun registrationCancelFail() {
        registration()

        request = createRegistrationCancelRequest(UUID.randomUUID())

        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.registrationCancel(request!!) }.error
        assertAll("Registration cancel Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Participant with this validation doesn't exists", errorResponse?.message, "error message") }
        )

        request = createRegistrationCancelRequest(participantValidationUUID)
        mockConferenceForCancelThrow()

        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.registrationCancel(request!!) }.error
        assertAll("Registration cancel Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Conference already started or finished", errorResponse?.message, "error message") }
        )


        request = createRegistrationCancelRequest(participantValidationUUID)
        mockConferenceForCancelThrow()
        mockParticipantForCancel(0)

        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.registrationCancel(request!!) }.error
        assertAll("Registration cancel Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Conference already started or finished", errorResponse?.message, "error message") }
        )
    }

    @Test
    fun feedback() {
        registration()

        request = createFeedbackRequest(participantValidationUUID, "Any text for feedback")
        mockFeedback(1)
        response = controller.feedback(request!!)

        assertAll("Feedback Success",
            { assertNotNull(response, "Response") },
            { assertEquals(participantValidationUUID, response?.validationUUID, "validationUUID") },
            { assertTrue(response?.feedbackResult!!, "feedbackResult") }
        )
    }

    @Test
    fun feedbackFail() {
        registration()

        request = createFeedbackRequest(participantValidationUUID, "Any text for feedback")
        mockFeedback(0)
        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.feedback(request!!) }.error
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

        request = createRequestForConferences(LocalDateTime.now().plusHours(60), LocalDateTime.now().plusHours(65))
        response = controller.availableConferences(request!!)

        assertAll("Available conferences",
            { assertNotNull(response, "Response") },
            { assertEquals(5, response?.conferenceAvailableList?.size, "conferenceAvailableList") }
        )

        for (conference in response?.conferenceAvailableList!!) {
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

        request = createRequestForConferences(LocalDateTime.now().plusHours(40), LocalDateTime.now().plusHours(45))
        errorResponse = assertThrows(PreconditionsFailedException::class.java) { controller.availableConferences(request!!) }.error
        assertAll("Available conferences Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("No conferences is available at this time period", errorResponse?.message, "error message") }
        )

        request = createRequestForConferences(LocalDateTime.now().plusHours(60), LocalDateTime.now().plusHours(59))
        errorResponse = assertThrows(BadRequestException::class.java) { controller.availableConferences(request!!) }.error
        assertAll("Available conferences Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(400, errorResponse?.code, "error code") },
            { assertEquals("Requested time isn't logical", errorResponse?.message, "error message") }
        )

        request = createRequestForConferences(LocalDateTime.now().minusHours(60), LocalDateTime.now().plusHours(65))
        errorResponse = assertThrows(BadRequestException::class.java) { controller.availableConferences(request!!) }.error
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
        doReturn(result).whenever(participantDAO
            .feedback(any(), any()))
    }

    private fun mockConferenceForCancel() {
        whenever(conferenceDAO.isAvailableForCancel(any()))
            .thenReturn(Conference(conferenceUUID = UUID.randomUUID()))
    }

    private fun mockConferenceForCancelThrow() {
        doThrow(PreconditionsFailedException("Conference already started or finished"))
            .whenever(conferenceDAO.isAvailableForCancel(any()))
    }

    private fun mockParticipantForCancel(response: Int) {
        whenever(conferenceDAO.cancelRegistration(any())).thenReturn(response)
    }

    private fun mockRegistration() {
        whenever(conferenceDAO.registerParticipant(any(), any()))
            .thenReturn(1)
    }
}