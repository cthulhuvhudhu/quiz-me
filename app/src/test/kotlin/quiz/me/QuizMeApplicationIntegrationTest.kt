package quiz.me

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.http.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import quiz.me.controller.QuizController
import quiz.me.controller.RestResponseEntityExceptionHandler
import quiz.me.model.dto.QuizDTO
import quiz.me.model.dto.UserDTO
import quiz.me.model.TestUtils.JacksonPage
import quiz.me.model.dto.CreateQuizDTO
import quiz.me.model.typeReference

@Sql(scripts = ["classpath:schema.sql"])
@SpringBootTest(
    classes = [QuizMe::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Transactional
@ContextConfiguration(classes = [SecurityConfig::class, ApplicationConfig::class,
    RestResponseEntityExceptionHandler::class])
@Import(SecurityConfig::class)
class QuizMeApplicationIntegrationTest {
    // tODO need test container?
    // todo tear down db when done? or in mem?

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private final val uri = "/api"
    private final val quizUri = "$uri/quizzes"
    private final val registrationUri = "$uri/register"

    private val defaultHeaders = HttpEntity<String>(HttpHeaders().apply {
        this.accept = listOf(MediaType.APPLICATION_JSON)
        this.contentType = MediaType.APPLICATION_JSON
    })

    private final val testUserA = UserDTO("a@a.com", "12345")
    private final val testUserB = UserDTO("b@a.com", "12345")

    @BeforeEach
    fun setUp() {
        registerUser(testUserA)
        registerUser(testUserB)
    }

    @Test
    fun `when GET quizzes RETURN empty`() {
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange(quizUri, HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body!!.content).isEmpty()
    }

    @Test
    fun `when GET quizzes RETURN 1 of 1 pages, not full`() {
        val createDTOs = (1..8).map {
            CreateQuizDTO(
                "Title_$it",
                "Text_$it",
                listOf("1$it", "2$it", "3$it", "4$it"),
                listOf(it % 4)
            )
        }
        val viewDTOs = createDTOs.mapIndexed { idx, it ->
            createQuiz(it, if (idx % 2 == 0) testUserA else testUserB)
        }

        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange(quizUri, HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isNotNull
        val page = result.body as PageImpl<QuizDTO>
        assertThat(page.isFirst).isTrue()
        assertThat(page.size).isEqualTo(QuizController.Companion.DEFAULT_PAGE_SIZE.toInt())
        assertThat(page.number).isEqualTo(0)
        assertThat(page.totalPages).isEqualTo(1)
        assertThat(page.numberOfElements).isEqualTo(8)
        assertThat(page.totalElements).isEqualTo(8)
        assertThat(page.content).containsExactlyElementsOf(viewDTOs)
    }

    @Test
    fun `when GET quizzes RETURN 2 of 3 pages`() {
        val createDTOs = (1..25).map {
            CreateQuizDTO(
                "Title_$it",
                "Text_$it",
                listOf("1$it", "2$it", "3$it", "4$it"),
                listOf(it % 4)
            )
        }
        val viewDTOs = createDTOs.mapIndexed { idx, it ->
            createQuiz(it, if (idx % 2 == 0) testUserA else testUserB)
        }

        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri?page=1", HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isNotNull
        val page = result.body as PageImpl<QuizDTO>
        assertThat(page.isFirst).isFalse()
        assertThat(page.number).isEqualTo(1)
        assertThat(page.totalPages).isEqualTo(3)
        assertThat(page.numberOfElements).isEqualTo(QuizController.Companion.DEFAULT_PAGE_SIZE.toInt())
        assertThat(page.totalElements).isEqualTo(25)
        assertThat(page.content).containsExactlyElementsOf(viewDTOs.drop(10).take(10))
    }

    @Test
    fun `when GET quiz does not exist RETURN not found`() {
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/1", HttpMethod.GET, defaultHeaders, ProblemDetail::class.java)
        assertThat(result).isNotNull()
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(result.body!!.detail).contains("Quiz not found for id = 1")
    }

    private fun registerUser(userDTO: UserDTO) {
        val user = Json.encodeToJsonElement(userDTO)
        val httpEntity = HttpEntity(user, defaultHeaders.headers)
        restTemplate.postForEntity(registrationUri, httpEntity, Nothing::class.java)
    }

    private fun createQuiz(quizDTO: CreateQuizDTO, author: UserDTO): QuizDTO {
        val quiz = Json.encodeToJsonElement(quizDTO)
        val httpEntity = HttpEntity(quiz, defaultHeaders.headers)

        return restTemplate
            .withBasicAuth(author.email, author.password)
            .postForEntity(quizUri, httpEntity, QuizDTO::class.java).body!!
    }
}