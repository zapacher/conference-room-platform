package ee.ctob.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import ee.ctob.access.ConferenceDAO
import ee.ctob.access.ParticipantDAO
import ee.ctob.access.RoomDAO
import ee.ctob.api.data.requests.AvailableConferenceRequest
import ee.ctob.api.data.requests.FeedbackRequest
import ee.ctob.api.data.requests.RegistrationCancelRequest
import ee.ctob.api.data.requests.RegistrationRequest
import ee.ctob.api.data.responses.AvailableConferenceListResponse
import ee.ctob.api.data.responses.FeedbackResponse
import ee.ctob.api.data.responses.RegistrationCancelResponse
import ee.ctob.api.data.responses.RegistrationResponse
import ee.ctob.api.error.ErrorResponse
import ee.ctob.api.error.PreconditionsFailedException
import ee.ctob.data.Conference
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
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
@AutoConfigureMockMvc
@SpringBootTest
class TestsMvcConference : TestContainer() {
    @MockBean
    lateinit var roomDAO: RoomDAO

    @MockBean
    lateinit var conferenceDAO: ConferenceDAO

    @SpyBean
    lateinit var participantDAO: ParticipantDAO

    @Autowired
    lateinit var mockMvc: MockMvc

    private var registrationRequest: RegistrationRequest? = null
    private var registrationCancelRequest: RegistrationCancelRequest? = null
    private var feedbackRequest: FeedbackRequest? = null
    private var availableConferenceRequest: AvailableConferenceRequest? = null
    private var registrationResponse: RegistrationResponse? = null
    private var registrationCancelResponse: RegistrationCancelResponse? = null
    private var feedbackResponse: FeedbackResponse? = null
    private var availableConferenceListResponse: AvailableConferenceListResponse? = null
    private var errorResponse: ErrorResponse? = null
    private var participantValidationUUID: UUID? = null

    @Test
    fun emptyRequest400() {

        performMvcThrow("/conference/registration/create", registrationRequest)
        assertAll(
            { assertNull(errorResponse, "errorResponse") },
            { assertNull(registrationResponse, "response") }
        )

        performMvcThrow("/conference/registration/cancel", registrationCancelRequest)
        assertAll(
            { assertNull(errorResponse, "errorResponse") },
            { assertNull(registrationCancelResponse, "response") }
        )

        performMvcThrow("/conference/feedback/create", feedbackRequest)
        assertAll(
            { assertNull(errorResponse, "errorResponse") },
            { assertNull(feedbackResponse, "response") }
        )

        performMvcThrow("/conference/available", availableConferenceRequest)
        assertAll(
            { assertNull(errorResponse, "errorResponse") },
            { assertNull(availableConferenceListResponse, "response") }
        )
    }

    @Test
    fun registration() {
        registrationRequest = createRegistrationRequest(UUID.randomUUID())
//        mockRegistration()
        registrationResponse = performMvc("/conference/registration/create", registrationRequest, RegistrationResponse::class.java)
        assertAll("Registration Success",
            { assertNotNull(registrationResponse, "Response") },
            { assertNotNull(registrationResponse?.validationUUID, "validationUUID")}
        )

        participantValidationUUID = registrationResponse?.validationUUID
    }

    @Test
    fun registrationFail() {
        registrationRequest = createRegistrationRequest(UUID.randomUUID())

        performMvcThrow("/conference/registration/create", registrationRequest)
        assertAll("Regostration Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Registration isn't available for this conference", errorResponse?.message, "error message") })
    }

    @Test
    fun registrationCancel() {
        registration()

        mockConferenceForCancel()
        mockParticipantForCancel(1)

        registrationCancelRequest = createRegistrationCancelRequest(participantValidationUUID)
        registrationCancelResponse = performMvc("/conference/registration/cancel", registrationCancelRequest, RegistrationCancelResponse::class.java)

        assertAll("Registration cancel success",
            { assertNotNull(registrationCancelResponse, "Response") },
            { assertNull(registrationCancelResponse?.validationUUID, "validationUUID") },
            { assertTrue(registrationCancelResponse?.registrationCancel!!, "registrationCancel") }
        )
    }

    @Test
    fun registrationCancelFail() {
        registration()

        registrationCancelRequest = createRegistrationCancelRequest(UUID.randomUUID())
        performMvcThrow("/conference/registration/cancel", registrationCancelRequest)
        assertAll("Registration cancel Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Participant with this validation doesn't exists", errorResponse?.message, "error message") }
        )

        registrationCancelRequest = createRegistrationCancelRequest(participantValidationUUID)
        mockConferenceForCancelThrow()
        performMvcThrow("/conference/registration/cancel", registrationCancelRequest)
        assertAll("Registration cancel Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Conference already started or finished", errorResponse?.message, "error message") }
        )

        registrationCancelRequest = createRegistrationCancelRequest(participantValidationUUID)
        mockConferenceForCancelThrow()
        mockParticipantForCancel(0)
        performMvcThrow("/conference/registration/cancel", registrationCancelRequest)
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
        feedbackResponse = performMvc("/conference/feedback/create", feedbackRequest, FeedbackResponse::class.java)
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
        performMvcThrow("/conference/feedback/create", feedbackRequest)
        assertAll("Feedback Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Feedback already exists or conference isn't finished", errorResponse?.message, "error message") }
        )
    }

    @Test
    fun availableConferences() {
        registration()

        val conferenceListSize = mockConferenceList()
        mockLocation()

        availableConferenceRequest = createRequestForConferences(LocalDateTime.now().plusHours(60), LocalDateTime.now().plusHours(65))
        availableConferenceListResponse = performMvc("/conference/available", availableConferenceRequest, AvailableConferenceListResponse::class.java)

        assertAll("Available conferences Fail",
            { assertNotNull(availableConferenceListResponse, "Response") },
            { assertEquals(conferenceListSize, availableConferenceListResponse?.conferenceAvailableList?.size, "conferenceAvailableList") }
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

        availableConferenceRequest = createRequestForConferences(LocalDateTime.now().plusHours(90), LocalDateTime.now().plusHours(100))
        performMvcThrow("/conference/available", availableConferenceRequest)
        assertAll("Available conferences Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("No conferences are available at this time period", errorResponse?.message, "error message") }
        )

        availableConferenceRequest = createRequestForConferences(LocalDateTime.now().plusHours(60), LocalDateTime.now().plusHours(59))
        performMvcThrow("/conference/available", availableConferenceRequest)
        assertAll("Available conferences Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(400, errorResponse?.code, "error code") },
            { assertEquals("Requested time isn't logical", errorResponse?.message, "error message") }
        )

        availableConferenceRequest = createRequestForConferences(LocalDateTime.now().minusHours(60), LocalDateTime.now().plusHours(65))
        performMvcThrow("/conference/available", availableConferenceRequest)
        assertAll("Available conferences Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(400, errorResponse?.code, "error code") },
            { assertEquals("Requested time isn't logical", errorResponse?.message, "error message") }
        )
    }

    private fun mockConferenceList(): Int {
        val conferenceList = getConferenceList()
        whenever(conferenceDAO.findAllAvailableBetween(any(), any()))
            .thenReturn(conferenceList)
        return conferenceList.size
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
            .thenReturn(Conference().apply {conferenceUUID = UUID.randomUUID() })
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

    private fun <T, R> performMvc(path: String, request: T, responseClass: Class<R>): R {
        val mapper = ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerModule(KotlinModule())
        }
        val responseMvc: String
        try {
            responseMvc = mockMvc.perform(
                post(path)
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            )
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON))
                .andReturn().response.contentAsString
            return mapper.readValue(responseMvc, responseClass)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun <T> performMvcThrow(path: String, request: T) {
        val mapper = ObjectMapper().apply {
            registerModule(JavaTimeModule())
        }
        try {
            val responseMvc = mockMvc.perform(
                post(path)
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            ).andReturn().response.contentAsString
            errorResponse = mapper.readValue(responseMvc, ErrorResponse::class.java)
        } catch (_: Exception) {
        }
    }
}