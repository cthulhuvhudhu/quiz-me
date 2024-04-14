package quiz.me.controller

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import quiz.me.OwnershipPermissionDeniedException
import quiz.me.QuizNotFoundException
import quiz.me.SecurityConfig
import quiz.me.SpringSecurityWebAuxTestConfig
import quiz.me.model.QuizTestModels
import quiz.me.model.UserTestModels
import quiz.me.model.dao.UserEntity
import quiz.me.model.dto.*
import quiz.me.service.QuizService
import quiz.me.service.UserQuizService

@WebMvcTest(QuizController::class)
@ContextConfiguration(classes = [SpringSecurityWebAuxTestConfig::class, SecurityConfig::class,
    RestResponseEntityExceptionHandler::class])
@Import(QuizController::class, SecurityConfig::class)
@WebAppConfiguration
class QuizControllerTest {
    @MockBean
    private lateinit var quizService: QuizService
    @MockBean
    private lateinit var userQuizService: UserQuizService
    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val uri = "/api/quizzes"
    private val mType = MediaType.APPLICATION_JSON

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @Test
    fun `test GET all quizzes no auth`() {
        mockMvc.get("$uri?page=0") {
        }.andExpect {
            status { isUnauthorized() }
        }
    }
    @Test
    @WithUserDetails("a@a.com")
    fun `test GET all quizzes empty`() {
        val pr = PageRequest.of(0, 2)
        `when`(quizService.getQuizzes(pr)).thenReturn(PageImpl(emptyList()))
        mockMvc.get("$uri?page=0") {
            accept = mType
        }.andExpect {
            status { isOk() }
            content { "" }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test GET all quizzes`() {
        val pr = PageRequest.of(0, 2)
        val expectedContent = QuizTestModels.quizzes.map { it.dto }.take(2)
        `when`(quizService.getQuizzes(pr)).thenReturn(PageImpl(expectedContent))

        mockMvc.get("$uri?page=0&size=2") {
            accept = mType
        }.andExpect {
            status { isOk() }
            content { jsonPath("$.totalPages", `is`(1)) }
            content { jsonPath("$.totalElements", `is`(2)) }
            content { jsonPath("$.content[0].id", `is`(expectedContent[0].id!!.toInt())) }
            content { jsonPath("$.content[0].title", `is`(expectedContent[0].title)) }
            content { jsonPath("$.content[0].text", `is`(expectedContent[0].text)) }
            content { jsonPath("$.content[0].options", `is`(expectedContent[0].options)) }
            content { jsonPath("$.content[1].id", `is`(expectedContent[1].id!!.toInt())) }
            content { jsonPath("$.content[1].title", `is`(expectedContent[1].title)) }
            content { jsonPath("$.content[1].text", `is`(expectedContent[1].text)) }
            content { jsonPath("$.content[1].options", `is`(expectedContent[1].options)) }
        }
    }


    @Test
    fun `test GET quiz by id no auth`() {
        QuizTestModels.quizzes.forEach {
            mockMvc.get("$uri/${it.id}") {
            }.andExpect {
                status { isUnauthorized() }
            }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test GET quiz by id`() {
        QuizTestModels.quizzes.forEach {
            `when`(quizService.getQuiz(it.id)).thenReturn(it.dto)
            mockMvc.get("$uri/${it.id}") {
                accept = mType
            }.andExpect {
                status { isOk() }
                content { Json.encodeToJsonElement(it.dto) }
            }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test GET quiz by id does not exist`() {
        val response = mockMvc.get("$uri/-1") {
            accept = mType
        }.andExpect {
            status { isNotFound() }
        }.andReturn()
        assertThat(response.resolvedException).isInstanceOf(ResponseStatusException::class.java)
        assertThat(response.resolvedException?.message).contains("Quiz not found for id = -1")
    }

    @Test
    fun `test GET completed quizzes by user no auth`() {
        mockMvc.get("$uri/completed?page=0&size=2") {
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test GET completed quizzes by user`() {
        val pr = PageRequest.of(0, 2)
        val testUser = UserTestModels.users.first()
        val testUserQuizzes = QuizTestModels.userQuizzes.filter { testUser.entityOut == it.user }

        val expectedContent = testUserQuizzes.map { it.userQuizDTO }
        `when`(userQuizService.findAllByUserEmail(testUser.email, pr)).thenAnswer {PageImpl(expectedContent) }
        mockMvc.get("$uri/completed?page=0&size=2") {
            accept = mType
        }.andExpect {
            status { isOk() }
            content { jsonPath("$.totalPages", `is`(1)) }
            content { jsonPath("$.totalElements", `is`(2)) }
            content { jsonPath("$.content[0].id", `is`(expectedContent[0].quizId!!.toInt())) }
            content { jsonPath("$.content[0].completedAt", containsString(expectedContent[0].completedAt.toString().dropLast(4))) }
            content { jsonPath("$.content[1].id", `is`(expectedContent[1].quizId!!.toInt())) }
            content { jsonPath("$.content[1].completedAt", containsString(expectedContent[1].completedAt.toString().dropLast(4))) }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test GET completed quizzes by user none`() {
        val pr = PageRequest.of(0, 2)

        `when`(userQuizService.findAllByUserEmail(UserTestModels.users.first().email, pr))
            .thenAnswer { PageImpl<ViewCompletedQuizDTO>(emptyList()) }
        mockMvc.get("$uri/completed?page=0&size=2") {
            accept = mType
        }.andExpect {
            status { isOk() }
            content { jsonPath("$.totalPages", `is`(1)) }
            content { jsonPath("$.totalElements", `is`(0)) }
            content { jsonPath("$.content", CoreMatchers.equalToObject(listOf<Any>())) }
        }
    }

    @Test
    fun `test POST add quiz valid no auth`() {
        val testQuiz = QuizTestModels.quizzes.first()

        mockMvc.post(uri) {
            contentType = mType
            content = Json.encodeToJsonElement(testQuiz.createDto)
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test POST add quiz valid`() {
        val testQuiz = QuizTestModels.quizzes.first()
        val testUser = UserTestModels.users.first()
        `when`(quizService.addQuiz(testQuiz.createDto, UserEntity(email = testUser.email)))
            .thenReturn(testQuiz.dto)

        mockMvc.post(uri) {
            contentType = mType
            content = Json.encodeToJsonElement(testQuiz.createDto)
            accept = mType
        }.andExpect {
            status { isOk() }
            content { Json.encodeToJsonElement(testQuiz.dto) }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test POST add quiz invalid`() {
        val testQuiz = CreateQuizDTO(options = listOf("1","2","3","4"))
        val res = mockMvc.post(uri) {
            contentType = mType
            content = Json.encodeToJsonElement(testQuiz)
            accept = mType
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertThat(res.resolvedException).isInstanceOf(MethodArgumentNotValidException::class.java)
        assertThat(res.resolvedException?.message).contains("Title is required for quiz")
        assertThat(res.resolvedException?.message).contains("Text is required for quiz")
    }

    @Test
    fun `test DELETE quiz no auth`() {
        mockMvc.delete("$uri/1") {
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test DELETE quiz not owner`() {
        `when`(quizService.deleteQuiz(1, "a@a.com")).thenThrow(OwnershipPermissionDeniedException::class.java)
        mockMvc.delete("$uri/1") {
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test DELETE quiz not exists`() {
        `when`(quizService.deleteQuiz(1, "a@a.com")).thenThrow(QuizNotFoundException::class.java)
        mockMvc.delete("$uri/1") {
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test DELETE quiz`() {
        mockMvc.delete("$uri/1") {
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `test POST check quiz answer success no auth`() {
        val guess = GuessDTO(listOf(1))
        mockMvc.post("$uri/1/solve") {
            contentType = mType
            content = Json.encodeToJsonElement(guess)
        }.andExpect {
            status { isUnauthorized() }
        }
    }
    @Test
    @WithUserDetails("a@a.com")
    fun `test POST check quiz answer success`() {
        val guess = GuessDTO(listOf(1))
        `when`(quizService.gradeQuiz(1, guess.answer, "a@a.com")).thenReturn(success)
        mockMvc.post("$uri/1/solve") {
            contentType = mType
            accept = mType
            content = Json.encodeToJsonElement(guess)
        }.andExpect {
            status { isOk() }
            content { Json.encodeToJsonElement(success) }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test POST check quiz answer failed`() {
        val guess = GuessDTO(listOf(2))
        `when`(quizService.gradeQuiz(1, guess.answer, "a@a.com")).thenReturn(failed)
        mockMvc.post("$uri/1/solve") {
            contentType = mType
            accept = mType
            content = Json.encodeToJsonElement(guess)
        }.andExpect {
            status { isOk() }
            content { Json.encodeToJsonElement(failed) }
        }
    }

    @Test
    @WithUserDetails("a@a.com")
    fun `test POST check quiz answer does not exist`() {
        val guess = GuessDTO(listOf(2))
        `when`(quizService.gradeQuiz(1, guess.answer, "a@a.com")).thenReturn(null)
        mockMvc.post("$uri/1/solve") {
            contentType = mType
            accept = mType
            content = Json.encodeToJsonElement(guess)
        }.andExpect {
            status { isNotFound() }
        }
    }
}
