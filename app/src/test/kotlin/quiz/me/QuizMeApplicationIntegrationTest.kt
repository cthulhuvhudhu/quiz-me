package quiz.me

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.http.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import quiz.me.controller.QuizController
import quiz.me.controller.RestResponseEntityExceptionHandler
import quiz.me.model.TestUtils.JacksonPage
import quiz.me.model.dto.*
import quiz.me.model.typeReference
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Sql(scripts = ["classpath:schema.sql"])
@SpringBootTest(
    classes = [QuizMe::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Transactional
@ContextConfiguration(classes = [SecurityConfig::class, ApplicationConfig::class,
    RestResponseEntityExceptionHandler::class])
@Import(SecurityConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QuizMeApplicationIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private val database = PostgreSQLContainer("postgres:latest")
        .withDatabaseName("integration-tests-db")
        .withUsername("test")
        .withPassword("test")

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

    @BeforeAll
    fun setup() {
        database.start()
    }

    @AfterAll
    fun teardown() {
        database.stop()
    }

    @BeforeEach
    fun setupEach() {
        registerUser(testUserA)
        registerUser(testUserB)
    }

    @Test
    fun `when GET quizzes no auth ERROR`() {
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
    fun `when GET quiz by id no auth ERROR`() {
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
        val errors = result.body!!.properties!!["errors"] as List<*>
        assertThat(errors).contains("Unable to find quiz '1'")
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
    fun `when GET completed quizzes no auth ERROR`() {
        var result = restTemplate.exchange("$quizUri/completed", HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<ViewCompletedQuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()

        result = restTemplate
            .withBasicAuth(testUserDNE.email, testUserDNE.password)
            .exchange("$quizUri/completed", HttpMethod.POST, defaultHeaders, typeReference<JacksonPage<ViewCompletedQuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(result.body).isNull()
    }

    @Test
    fun `when GET completed quizzes RETURN empty`() {
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/completed", HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<ViewCompletedQuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body!!.content).isEmpty()
    }

    @Test
    fun `when GET completed quizzes RETURN first page`() {
        val completedDTOs =  (1..8).map { writeQuiz(it, if (it % 2 == 0) testUserA else testUserB) }
            .map { ViewCompletedQuizDTO(it.second.id!!, solveQuiz(it.second.id!!, GuessDTO(it.first.answer), testUserA)) }
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/completed", HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<ViewCompletedQuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isNotNull
        val page = result.body as PageImpl<*>
        assertThat(page.isFirst).isTrue()
        assertThat(page.size).isEqualTo(QuizController.Companion.DEFAULT_PAGE_SIZE.toInt())
        assertThat(page.number).isEqualTo(0)
        assertThat(page.totalPages).isEqualTo(1)
        assertThat(page.numberOfElements).isEqualTo(8)
        assertThat(page.totalElements).isEqualTo(8)
        (1..8).forEach { i ->
            val actual = page.content.map { it as ViewCompletedQuizDTO }.first { it.quizId!!.toInt() == i }
            val expected = completedDTOs.first { it.quizId!!.toInt() == i }
            assertThat(actual.completedAt).isCloseTo(expected.completedAt, within(100, ChronoUnit.MILLIS))
        }
    }

    @Test
    fun `when GET completed quizzes RETURN 2 of 3 pages`() {
        val completedDTOs =  (1..25).map { writeQuiz(it, if (it % 2 == 0) testUserA else testUserB) }
            .map { ViewCompletedQuizDTO(it.second.id!!, solveQuiz(it.second.id!!, GuessDTO(it.first.answer), testUserA)) }
            .sortedByDescending { it.completedAt }
            .drop(10)
            .take(10)
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/completed?page=1", HttpMethod.GET, defaultHeaders, typeReference<JacksonPage<ViewCompletedQuizDTO>>())
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isNotNull
        val page = result.body as PageImpl<*>
        assertThat(page.isFirst).isFalse()
        assertThat(page.number).isEqualTo(1)
        assertThat(page.totalPages).isEqualTo(3)
        assertThat(page.numberOfElements).isEqualTo(QuizController.Companion.DEFAULT_PAGE_SIZE.toInt())
        assertThat(page.totalElements).isEqualTo(25)
        val actual = page.content.map { it as ViewCompletedQuizDTO }
        (0..9).forEach { i ->
            assertThat(actual[i].completedAt).isCloseTo(completedDTOs[i].completedAt, within(100, ChronoUnit.MILLIS))
        }
    }

    // TODO back out properties file

    @Test
    fun `when POST add quiz no auth ERROR`() {
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
    fun `when POST invalid quiz ERROR bad request`() {
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
    fun `when DELETE quiz no auth ERROR`() {
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
    fun `when DELETE quiz does not exist ERROR not found`() {
        val result = restTemplate
            .withBasicAuth(testUserA.email, testUserA.password)
            .exchange("$quizUri/1", HttpMethod.DELETE, defaultHeaders, ProblemDetail::class.java)
        assertThat(result).isNotNull
        assertThat(result!!.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        val errors = result.body!!.properties!!["errors"] as List<*>
        assertThat(errors).containsOnly("Unable to find quiz '1'")
    }

    @Test
    fun `when DELETE quiz unauthorized ERROR forbidden`() {
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
    fun `when POST check quiz answer no auth ERROR`() {
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
    fun `when POST solution quiz does not exist ERROR not found`() {
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
    fun `when POST invalid body solution ERROR bad request`() {
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
    @Test
    fun `when POST invalid user registration ERROR`() {
        val user = Json.encodeToJsonElement(UserDTO("notanemail", "1234"))
        val httpEntity = HttpEntity(user, defaultHeaders.headers)
        val expected = listOf("Email is not valid", "Password must be 5 characters or more")
        val result = restTemplate.postForEntity(registrationUri, httpEntity, ProblemDetail::class.java)
        assertThat(result).isNotNull
        assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        val errors = result.body!!.properties!!["errors"] as List<*>
        assertThat(errors).containsOnly(*expected.toTypedArray())
    }

    @Test
    fun `when POST duplicate user registration ERROR`() {
        val user = Json.encodeToJsonElement(testUserA)
        val httpEntity = HttpEntity(user, defaultHeaders.headers)
        val result = restTemplate.postForEntity(registrationUri, httpEntity, ProblemDetail::class.java)
        assertThat(result).isNotNull
        assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        val errors = result.body!!.properties!!["errors"] as List<*>
        assertThat(errors).containsExactly("Registration is not permitted at this time. Please try again later or with different credentials.")
    }

    @Test
    fun `when POST user registration RETURN`() {
        val user = Json.encodeToJsonElement(testUserDNE)
        val httpEntity = HttpEntity(user, defaultHeaders.headers)
        val result = restTemplate.postForEntity(registrationUri, httpEntity, Nothing::class.java)
        assertThat(result).isNotNull
        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
    }

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

    private fun solveQuiz(id: Long, guess: GuessDTO, solver: UserDTO): LocalDateTime {
        val httpEntity = HttpEntity(guess, defaultHeaders.headers)
        restTemplate
            .withBasicAuth(solver.email, solver.password)
            .postForEntity("$quizUri/$id/solve", httpEntity, FeedbackDTO::class.java).body!!
        return LocalDateTime.now()
    }

    private fun createQuiz(quizDTO: CreateQuizDTO, author: UserDTO): QuizDTO {
        val quiz = Json.encodeToJsonElement(quizDTO)
        val httpEntity = HttpEntity(quiz, defaultHeaders.headers)

        return restTemplate
            .withBasicAuth(author.email, author.password)
            .postForEntity(quizUri, httpEntity, QuizDTO::class.java).body!!
    }
}