package quiz.me

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Page
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import quiz.me.controller.RestResponseEntityExceptionHandler
import quiz.me.model.dto.CreateQuizDTO
import quiz.me.model.dto.QuizDTO
import java.nio.charset.Charset


@SpringBootTest(
    classes = [QuizMe::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(classes = [SecurityConfig::class, ApplicationConfig::class,
    RestResponseEntityExceptionHandler::class])
@Import(SecurityConfig::class)
class QuizMeApplicationIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private final val uri = "/api"
    private final val quizUri = "$uri/quizzes"
    private final val registrationUri = "$uri/registration"

    private val defaultHeaders = HttpEntity<String>(HttpHeaders().apply {
        this.accept = listOf(MediaType.APPLICATION_JSON)
        this.contentType = MediaType.APPLICATION_JSON
    })

    private final val addQuizDTO = CreateQuizDTO("title", "text", listOf("1", "2", "3", "4"), listOf(1))
//    @Test
//    fun `when GET quizzes RETURN empty`() {
//        // tODO REgister
//        // Add header
//        val result = restTemplate.exchange(quizUri, HttpMethod.GET, defaultHeaders, typeReference<Page<QuizDTO>>())
//        assertThat(result).isNotNull()
//        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
//        assertThat(result.body.toString()).isEqualTo("{}")
//    }

    @Test
    fun `when GET quizzes no auth`() {
        var result = restTemplate.exchange(quizUri, HttpMethod.GET, defaultHeaders, typeReference<Page<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()

        val headers = createAuthHeader("dne@a.com", "password")
        val httpEntity = HttpEntity<String>(headers)
        result = restTemplate.exchange(quizUri, HttpMethod.GET, httpEntity, typeReference<Page<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()
    }

    @Test
    fun `when GET quiz by id no auth`() {
        var result = restTemplate.exchange("$quizUri/1", HttpMethod.GET, null, QuizDTO::class.java)
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()

        val headers = createAuthHeader("dne@a.com", "password")
        val httpEntity = HttpEntity<String>(headers)
        result = restTemplate.exchange("$quizUri/1", HttpMethod.GET, httpEntity, QuizDTO::class.java)
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()
    }

    @Test
    fun `when POST add quiz no auth`() {
        val body = Json.encodeToJsonElement(addQuizDTO).toString()
        var httpEntity = HttpEntity(body, defaultHeaders.headers)
        var result = restTemplate.exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<Page<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()

        httpEntity = HttpEntity(body, createAuthHeader("dne@a.com", "password"))
        result = restTemplate.exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<Page<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()
    }

    @Test
    fun `when DELETE quiz no auth`() {
        val quizUri = "$quizUri/1"
        val body = Json.encodeToJsonElement(addQuizDTO).toString()
        var httpEntity = HttpEntity(body, defaultHeaders.headers)
        var result = restTemplate.exchange(quizUri, HttpMethod.DELETE, httpEntity, typeReference<Page<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()

        httpEntity = HttpEntity(body, createAuthHeader("dne@a.com", "password"))
        result = restTemplate.exchange(quizUri, HttpMethod.DELETE, httpEntity, typeReference<Page<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()
    }

    @Test
    fun `when POST check quiz answer no auth`() {
        val quizUri = "$quizUri/1/solve"
        val body = Json.encodeToJsonElement(addQuizDTO).toString()
        var httpEntity = HttpEntity(body, defaultHeaders.headers)
        var result = restTemplate.exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<Page<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()

        httpEntity = HttpEntity(body, createAuthHeader("dne@a.com", "password"))
        result = restTemplate.exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<Page<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()
    }

    private inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}
    private fun createAuthHeader(username: String, password: String): HttpHeaders {
        return HttpHeaders().apply {
            this.accept = listOf(MediaType.APPLICATION_JSON)
            this.contentType = MediaType.APPLICATION_JSON
            this.setBasicAuth(username, password, Charset.defaultCharset())
        }
    }
}