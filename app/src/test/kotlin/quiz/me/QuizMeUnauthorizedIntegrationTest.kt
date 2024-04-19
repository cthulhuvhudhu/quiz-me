package quiz.me

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.test.context.jdbc.Sql
import quiz.me.model.TestUtils
import quiz.me.model.dto.CreateQuizDTO
import quiz.me.model.dto.QuizDTO
import quiz.me.model.typeReference

@SpringBootTest(
    classes = [QuizMe::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Sql(scripts = ["classpath:schema.sql"])
class QuizMeUnauthorizedIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate
    @Autowired
    lateinit var testUtils: TestUtils

    private final val uri = "/api"
    private final val quizUri = "$uri/quizzes"

    private val defaultHeaders = HttpEntity<String>(HttpHeaders().apply {
        this.accept = listOf(MediaType.APPLICATION_JSON)
        this.contentType = MediaType.APPLICATION_JSON
    })

    private final val testUsername = "dne@a.com"
    private final val testPassword = "password9999"
    private final val addQuizDTO = CreateQuizDTO("title", "text", listOf("1", "2", "3", "4"), listOf(1))

    @Test
    fun `when GET quizzes no auth`() {
        var result = restTemplate.exchange(quizUri, HttpMethod.GET, defaultHeaders, typeReference<TestUtils.JacksonPage<QuizDTO>>())
        Assertions.assertThat(result).isNotNull()
        Assertions.assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        Assertions.assertThat(result.body).isNull()

        val headers = testUtils.createAuthHeader(testUsername, testPassword)
        val httpEntity = HttpEntity<String>(headers)
        result = restTemplate.exchange(quizUri, HttpMethod.GET, httpEntity, typeReference<TestUtils.JacksonPage<QuizDTO>>())
        Assertions.assertThat(result).isNotNull()
        Assertions.assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        Assertions.assertThat(result.body).isNull()
    }

    @Test
    fun `when GET quiz by id no auth`() {
        var result = restTemplate.exchange("$quizUri/1", HttpMethod.GET, null, QuizDTO::class.java)
        Assertions.assertThat(result).isNotNull()
        Assertions.assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        Assertions.assertThat(result.body).isNull()

        val headers = testUtils.createAuthHeader(testUsername, testPassword)
        val httpEntity = HttpEntity<String>(headers)
        result = restTemplate.exchange("$quizUri/1", HttpMethod.GET, httpEntity, QuizDTO::class.java)
        Assertions.assertThat(result).isNotNull()
        Assertions.assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        Assertions.assertThat(result.body).isNull()
    }

    @Test
    fun `when POST add quiz no auth`() {
        val body = Json.encodeToJsonElement(addQuizDTO).toString()
        var httpEntity = HttpEntity(body, defaultHeaders.headers)
        var result = restTemplate.exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<TestUtils.JacksonPage<QuizDTO>>())
        Assertions.assertThat(result).isNotNull()
        Assertions.assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        Assertions.assertThat(result.body).isNull()

        httpEntity = HttpEntity(body, testUtils.createAuthHeader(testUsername, testPassword))
        result = restTemplate.exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<TestUtils.JacksonPage<QuizDTO>>())
        Assertions.assertThat(result).isNotNull()
        Assertions.assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        Assertions.assertThat(result.body).isNull()
    }

    @Test
    fun `when DELETE quiz no auth`() {
        val quizUri = "$quizUri/1"
        val body = Json.encodeToJsonElement(addQuizDTO).toString()
        var httpEntity = HttpEntity(body, defaultHeaders.headers)
        var result = restTemplate.exchange(quizUri, HttpMethod.DELETE, httpEntity, typeReference<TestUtils.JacksonPage<QuizDTO>>())
        Assertions.assertThat(result).isNotNull()
        Assertions.assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        Assertions.assertThat(result.body).isNull()

        httpEntity = HttpEntity(body, testUtils.createAuthHeader(testUsername, testPassword))
        result = restTemplate.exchange(quizUri, HttpMethod.DELETE, httpEntity, typeReference<TestUtils.JacksonPage<QuizDTO>>())
        Assertions.assertThat(result).isNotNull()
        Assertions.assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        Assertions.assertThat(result.body).isNull()
    }

    @Test
    fun `when POST check quiz answer no auth`() {
        val quizUri = "$quizUri/1/solve"
        val body = Json.encodeToJsonElement(addQuizDTO).toString()
        var httpEntity = HttpEntity(body, defaultHeaders.headers)
        var result = restTemplate.exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<TestUtils.JacksonPage<QuizDTO>>())
        Assertions.assertThat(result).isNotNull()
        Assertions.assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        Assertions.assertThat(result.body).isNull()

        httpEntity = HttpEntity(body, testUtils.createAuthHeader(testUsername, testPassword))
        result = restTemplate.exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<TestUtils.JacksonPage<QuizDTO>>())
        Assertions.assertThat(result).isNotNull()
        Assertions.assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        Assertions.assertThat(result.body).isNull()
    }
}