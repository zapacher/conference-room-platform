package ee.ctob.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import ee.ctob.access.ConferenceDAO
import ee.ctob.access.ParticipantDAO
import ee.ctob.access.RoomDAO
import ee.ctob.access.data.Conference
import ee.ctob.api.Request
import ee.ctob.api.Response
import ee.ctob.api.error.ErrorResponse
import ee.ctob.api.error.PreconditionsFailedException
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
import testUtils.ObjectCreators.Companion.createFeedbackRequest
import testUtils.ObjectCreators.Companion.createRegistrationCancelRequest
import testUtils.ObjectCreators.Companion.createRegistrationRequest
import testUtils.ObjectCreators.Companion.createRequestForConferences
import testUtils.ObjectCreators.Companion.getConferenceList
import testUtils.TestContainer
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

    private var request: Request? = null
    private var response: Response? = null
    private var errorResponse: ErrorResponse? = null
    private var participantValidationUUID: UUID? = null

    @Test
    fun emptyRequest400() {
        request = Request(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )

        performMvcThrow("/conference/registration/create")
        assertAll(
            { assertNull(errorResponse, "errorResponse") },
            { assertNull(response, "response") }
        )

        performMvcThrow("/conference/registration/cancel")
        assertAll(
            { assertNull(errorResponse, "errorResponse") },
            { assertNull(response, "response") }
        )

        performMvcThrow("/conference/feedback/create")
        assertAll(
            { assertNull(errorResponse, "errorResponse") },
            { assertNull(response, "response") }
        )

        performMvcThrow("/conference/available")
        assertAll(
            { assertNull(errorResponse, "errorResponse") },
            { assertNull(response, "response") }
        )
    }

    @Test
    fun registration() {
        request = createRegistrationRequest(UUID.randomUUID())
        mockRegistration()
        performMvc("/conference/registration/create")
        assertAll("Registration Success",
            { assertNotNull(response, "Response") },
            { assertNotNull(response?.validationUUID, "validationUUID")}
        )

        participantValidationUUID = response?.validationUUID
    }

    @Test
    fun registrationFail() {
        request = createRegistrationRequest(UUID.randomUUID())

        performMvcThrow("/conference/registration/create")
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
        request = createRegistrationCancelRequest(participantValidationUUID)
        performMvc("/conference/registration/cancel")
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
        performMvcThrow("/conference/registration/cancel")
        assertAll("Registration cancel Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Participant with this validation doesn't exists", errorResponse?.message, "error message") }
        )

        request = createRegistrationCancelRequest(participantValidationUUID)
        mockConferenceForCancelThrow()
        performMvcThrow("/conference/registration/cancel")
        assertAll("Registration cancel Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Conference already started or finished", errorResponse?.message, "error message") }
        )

        request = createRegistrationCancelRequest(participantValidationUUID)
        mockConferenceForCancelThrow()
        mockParticipantForCancel(0)
        performMvcThrow("/conference/registration/cancel")
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
        performMvc("/conference/feedback/create")
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
        performMvcThrow("/conference/feedback/create")
        assertAll("Feedback Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code, "error code") },
            { assertEquals("Feedback already exists or conference isn't finished", errorResponse?.message, "error message") }
        )
    }

    @Test
    fun availableConferences() {
        registration()

        mockConferenceList()
        mockLocation()

        request = createRequestForConferences(LocalDateTime.now().plusHours(60), LocalDateTime.now().plusHours(65))
        performMvc("/conference/available")

        assertAll("Available conferences Fail",
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
        performMvcThrow("/conference/available")
        assertAll("Available conferences Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(100, errorResponse?.code!!, "error code") },
            { assertEquals("No conferences is available at this time period", errorResponse?.message!!, "error message") }
        )

        request = createRequestForConferences(LocalDateTime.now().plusHours(60), LocalDateTime.now().plusHours(59))
        performMvcThrow("/conference/available")
        assertAll("Available conferences Fail",
            { assertNotNull(errorResponse, "ErrorResponse") },
            { assertEquals(400, errorResponse?.code, "error code") },
            { assertEquals("Requested time isn't logical", errorResponse?.message, "error message") }
        )

        request = createRequestForConferences(LocalDateTime.now().minusHours(60), LocalDateTime.now().plusHours(65))
        performMvcThrow("/conference/available")
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

    private fun performMvc(path: String) {
        val mapper = ObjectMapper()
        mapper.registerModule(JavaTimeModule())
        val responseMvc: String
        try {
            responseMvc = mockMvc.perform(
                post(path)
                    .contentType(APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            )
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON))
                .andReturn().response.contentAsString
            response = mapper.readValue(responseMvc, Response::class.java)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun performMvcThrow(path: String) {
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