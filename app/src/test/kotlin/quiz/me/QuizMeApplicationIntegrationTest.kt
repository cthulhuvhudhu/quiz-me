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
import quiz.me.model.TestUtils.JacksonPage
import quiz.me.model.dto.*
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
    private final val testUserDNE = UserDTO("dne@a.com", "password9999")

    @BeforeEach
    fun setUp() {
        registerUser(testUserA)
        registerUser(testUserB)
    }

    @Test
    fun `when GET quizzes no auth`() {
        var result = restTemplate.exchange(quizUri, HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()

        result = restTemplate
            .withBasicAuth(testUserDNE.email, testUserDNE.password)
            .exchange(quizUri, HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()
    }

    @Test
    fun `when GET quizzes RETURN empty`() {
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange(quizUri, HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body!!.content).isEmpty()
    }

    @Test
    fun `when GET quizzes RETURN 1 of 1 pages, not full`() {
        val viewDTOs = (1..8).map { writeQuiz(it, if (it % 2 == 0) testUserA else testUserB) }
            .map { it.second }

        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange(quizUri, HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull
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
        val viewDTOs = (1..25)
            .map { writeQuiz(it, if (it % 2 == 0) testUserA else testUserB) }
            .map { it.second }

        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri?page=1", HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull
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
    fun `when GET quiz by id no auth`() {
        var result = restTemplate.exchange("$quizUri/1", HttpMethod.GET, defaultHeaders, QuizDTO::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()

        result = restTemplate
            .withBasicAuth(testUserDNE.email, testUserDNE.password)
            .exchange("$quizUri/1", HttpMethod.GET, defaultHeaders, QuizDTO::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()
    }

    @Test
    fun `when GET quiz does not exist RETURN not found`() {
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/1", HttpMethod.GET, defaultHeaders, ProblemDetail::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(result.body!!.detail).contains("Quiz not found for id = 1")
    }

    @Test
    fun `when GET quiz RETURN`() {
        val viewDTO = writeQuiz().second
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/${viewDTO.id!!}", HttpMethod.GET, defaultHeaders, QuizDTO::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isNotNull
        assertThat(result.body).isEqualTo(viewDTO)
    }

    @Test
    fun `when POST add quiz no auth`() {
        val addQuizDTO = CreateQuizDTO("title", "text", listOf("1", "2", "3", "4"), listOf(1))
        val body = Json.encodeToJsonElement(addQuizDTO).toString()
        val httpEntity = HttpEntity(body, defaultHeaders.headers)
        var result = restTemplate.exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()

        result = restTemplate
            .withBasicAuth(testUserDNE.email, testUserDNE.password)
            .exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()
    }

    @Test
    fun `when POST invalid quiz RETURN bad request`() {
        val createDTO = CreateQuizDTO(
            options = listOf("1"),
            answer = listOf(1)
        )

        val expectedErrors = listOf(
            "Title is required for quiz",
            "Text is required for quiz",
            "At least two answer options are required for quiz",
        )

        val quiz = Json.encodeToJsonElement(createDTO)
        val httpEntity = HttpEntity(quiz, defaultHeaders.headers)

        val result = restTemplate
            .withBasicAuth(testUserB.email, testUserB.password)
            .postForEntity(quizUri, httpEntity, ProblemDetail::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(result.body).isNotNull
        val errors = result.body!!.properties!!["errors"] as List<*>
        assertThat(errors).contains(*expectedErrors.toTypedArray())
    }

    @Test
    fun `when POST quiz RETURN`() {
        val createDTO = CreateQuizDTO(
            "Title_1",
            "Text_1",
            listOf("1", "2", "3", "4"),
            listOf(1)
        )

        val quiz = Json.encodeToJsonElement(createDTO)
        val httpEntity = HttpEntity(quiz, defaultHeaders.headers)

        val result = restTemplate
            .withBasicAuth(testUserB.email, testUserB.password)
            .postForEntity(quizUri, httpEntity, QuizDTO::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isNotNull
        val quizDTO = result.body!!
        assertThat(quizDTO.title).isEqualTo(createDTO.title)
        assertThat(quizDTO.text).isEqualTo(createDTO.text)
        assertThat(quizDTO.options).isEqualTo(createDTO.options)
        assertThat(quizDTO.id).isNotNull
    }

    @Test
    fun `when DELETE quiz no auth`() {
        val quizUri = "$quizUri/1"
        var result = restTemplate.exchange(quizUri, HttpMethod.DELETE, defaultHeaders, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()

        result = restTemplate
            .withBasicAuth(testUserDNE.email, testUserDNE.password)
            .exchange(quizUri, HttpMethod.DELETE, defaultHeaders, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()
    }

    @Test
    fun `when DELETE quiz does not exist RETURN not found`() {
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/1", HttpMethod.DELETE, defaultHeaders, ProblemDetail::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        val errors = result.body!!.properties!!["errors"] as List<*>
        assertThat(errors).containsOnly("Unable to find quiz '1'")
    }

    @Test
    fun `when DELETE quiz unauthorized RETURN forbidden`() {
        val viewDTO = writeQuiz(1, testUserB).second

        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/${viewDTO.id!!}", HttpMethod.DELETE, defaultHeaders, ProblemDetail::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        val errors = result.body!!.properties!!["errors"] as List<*>
        assertThat(errors).containsOnly("You do not have permission to delete the quiz")
    }

    @Test
    fun `when DELETE quiz RETURN`() {
        val viewDTO = writeQuiz().second

        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/${viewDTO.id!!}", HttpMethod.DELETE, defaultHeaders, Object::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(result.body).isNull()
    }

    @Test
    fun `when POST check quiz answer no auth`() {
        val quizUri = "$quizUri/1/solve"
        val addQuizDTO = CreateQuizDTO("title", "text", listOf("1", "2", "3", "4"), listOf(1))
        val body = Json.encodeToJsonElement(addQuizDTO).toString()
        val httpEntity = HttpEntity(body, defaultHeaders.headers)
        var result = restTemplate.exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()

        result = restTemplate
            .withBasicAuth(testUserDNE.email, testUserDNE.password)
            .exchange(quizUri, HttpMethod.POST, httpEntity, typeReference<JacksonPage<QuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()
    }

    @Test
    fun `when POST solution quiz does not exist RETURN not found`() {
        val guessDTO = GuessDTO(listOf(1))
        val httpEntity = HttpEntity(Json.encodeToJsonElement(guessDTO), defaultHeaders.headers)
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/1/solve", HttpMethod.POST, httpEntity, ProblemDetail::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        val errors = result.body!!.properties!!["errors"] as List<*>
        assertThat(errors).containsOnly("Unable to find quiz '1'")
    }

    @Test
    fun `when POST incorrect solution RETURN failed`() {
        val quizDTOs = writeQuiz()
        val guessDTO = GuessDTO(listOf((0..3).first { it !in quizDTOs.first.answer}))
        val httpEntity = HttpEntity(Json.encodeToJsonElement(guessDTO), defaultHeaders.headers)
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/${quizDTOs.second.id!!}/solve", HttpMethod.POST, httpEntity, FeedbackDTO::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isEqualTo(failed)
    }

    @Test
    fun `when POST correct solution RETURN success`() {
        val quizDTOs = writeQuiz()
        val guessDTO = GuessDTO(quizDTOs.first.answer)
        val httpEntity = HttpEntity(Json.encodeToJsonElement(guessDTO), defaultHeaders.headers)
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/${quizDTOs.second.id!!}/solve", HttpMethod.POST, httpEntity, FeedbackDTO::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isEqualTo(success)
    }

    @Test
    fun `when POST invalid body solution RETURN bad request`() {
        val quizDTOs = writeQuiz()
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/${quizDTOs.second.id!!}/solve", HttpMethod.POST, defaultHeaders, ProblemDetail::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        val errors = result.body!!.properties!!["errors"] as List<*>
        assertThat(errors.size).isEqualTo(1)
        assertThat(errors[0].toString()).contains("Required request body")
    }

    // TODO
    //completed
    // ... consider E2E
    // ... we have TODOs

    private fun registerUser(userDTO: UserDTO) {
        val user = Json.encodeToJsonElement(userDTO)
        val httpEntity = HttpEntity(user, defaultHeaders.headers)
        restTemplate.postForEntity(registrationUri, httpEntity, Nothing::class.java)
    }

    private fun writeQuiz(suffix: Int = 1, author: UserDTO = testUserA): Pair<CreateQuizDTO, QuizDTO> {
        val createDTO = CreateQuizDTO(
            "Title_$suffix",
            "Text_$suffix",
            listOf("1", "2", "3", "4"),
            listOf(1)
        )
        return createDTO to createQuiz(createDTO, author)
    }

    private fun createQuiz(quizDTO: CreateQuizDTO, author: UserDTO): QuizDTO {
        val quiz = Json.encodeToJsonElement(quizDTO)
        val httpEntity = HttpEntity(quiz, defaultHeaders.headers)

        return restTemplate
            .withBasicAuth(author.email, author.password)
            .postForEntity(quizUri, httpEntity, QuizDTO::class.java).body!!
    }
}