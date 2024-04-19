package quiz.me

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import quiz.me.controller.RestResponseEntityExceptionHandler
import quiz.me.model.TestUtils
import quiz.me.model.dto.QuizDTO
import quiz.me.model.dto.UserDTO
import quiz.me.model.TestUtils.JacksonPage
import quiz.me.model.typeReference

@Sql(scripts = ["classpath:schema.sql"])
@SpringBootTest(
    classes = [QuizMe::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Transactional//(propagation = Propagation.NOT_SUPPORTED)
@ContextConfiguration(classes = [SecurityConfig::class, ApplicationConfig::class,
    RestResponseEntityExceptionHandler::class])
@Import(SecurityConfig::class)
class QuizMeApplicationIntegrationTest {
    // tODO need test container?
    // todo tear down db when done? or in mem?

    @Autowired
    lateinit var restTemplate: TestRestTemplate
    @Autowired
    lateinit var testUtils: TestUtils

    private final val uri = "/api"
    private final val quizUri = "$uri/quizzes"
    private final val registrationUri = "$uri/register"

    private val defaultHeaders = HttpEntity<String>(HttpHeaders().apply {
        this.accept = listOf(MediaType.APPLICATION_JSON)
        this.contentType = MediaType.APPLICATION_JSON
    })

    private final val testUserA = UserDTO("a@a.com", "12345")
    private final val testUserB = UserDTO("b@a.com", "12345")

    @Test
    fun `when GET quizzes RETURN empty`() {
        registerUser(testUserA)
        val headers = testUtils.createAuthHeader(testUserA.email, testUserA.password)
        val httpEntity = HttpEntity<String>(headers)

        val result = restTemplate.exchange("$quizUri?page=0", HttpMethod.GET, httpEntity, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body!!.content).isEmpty()
    }

    private fun registerUser(user: UserDTO) = registerUser(user.email, user.password)
    private fun registerUser(username: String, password: String) {
        val user = Json.encodeToJsonElement(UserDTO(username,  password))
        val httpEntity = HttpEntity(user, defaultHeaders.headers)
        restTemplate.postForEntity(registrationUri, httpEntity, Nothing::class.java)
    }
}