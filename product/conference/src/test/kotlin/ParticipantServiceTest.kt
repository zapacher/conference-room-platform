import ee.ctob.Application
import ee.ctob.access.ConferenceDAO
import ee.ctob.api.Request
import ee.ctob.api.enums.Gender
import ee.ctob.service.ParticipantService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import testUtils.TestContainer
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(classes = [Application::class])
@AutoConfigureMockMvc
class ParticipantServiceTest : TestContainer() {

    @Autowired
    lateinit var participantService: ParticipantService
    @MockBean
    lateinit var conferenceDAO: ConferenceDAO

    @Test
    fun testRegistration() {
//        assertNotNull(participantService.conferenceDAO)
        // Define mock behavior for conferenceDAO
        val conferenceUUID = UUID.randomUUID()
        Mockito.`when`(conferenceDAO.registerParticipant(UUID.randomUUID(), conferenceUUID))
            .thenReturn(1)  // Or any appropriate return value

        // Call the service method
        var response = participantService.registration(
            Request(
                from = LocalDateTime.now().minusDays(1),
                until = LocalDateTime.now(),
                firstName = "Chuck",
                lastName = "Norris",
                gender = Gender.MALE,
                email = "chuck.norris@gmail.com",
                dateOfBirth = LocalDateTime.of(1940, 4, 10, 0, 0, 0, 0),
                conferenceUUID = conferenceUUID,
                validationUUID = UUID.randomUUID(),
                feedback = "Looking forward to the event"
            )
        )

        // Print or assert the response
        println(response)
    }
}