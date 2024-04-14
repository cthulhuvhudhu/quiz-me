package quiz.me.controller

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import quiz.me.SecurityConfig
import quiz.me.model.UserTestModels
import quiz.me.model.dto.UserDTO
import quiz.me.service.UserService
import org.springframework.test.web.servlet.MvcResult
import org.springframework.web.bind.MethodArgumentNotValidException
import quiz.me.RegistrationDeniedException
import quiz.me.SpringSecurityWebAuxTestConfig

@WebMvcTest(RegistrationController::class)
@ContextConfiguration(classes = [SpringSecurityWebAuxTestConfig::class, SecurityConfig::class,
    RestResponseEntityExceptionHandler::class])
@Import(RegistrationController::class)
class RegistrationControllerTest {
    @MockBean
    private lateinit var userService: UserService

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val url = "/api/register"
    private val mType = MediaType.APPLICATION_JSON

    @Test
    fun `test POST register valid user dto returns valid ResponseEntity`() {
        val testUser = UserTestModels.users.first()
        `when`(userService.registerUser(testUser.email, testUser.dto.password))
            .thenReturn(testUser.entityOut)

        mockMvc.post(url) {
            contentType = mType
            content = Json.encodeToJsonElement(testUser.dto)
        }.andExpect {
            status { isOk() }
            content { "" }
        }
    }

    @Test
    fun `test POST register invalid email returns client error`() {
        val testUser = UserDTO("notemail", "12345")
        val response = doTest(Json.encodeToJsonElement(testUser))
        assertThat(response.resolvedException).isInstanceOf(MethodArgumentNotValidException::class.java)
        assertThat(response.resolvedException?.message).contains("Email is not valid")
    }

    @Test
    fun `test POST register invalid password returns client error`() {
        val testUser = UserDTO("a@a.com", "1234")
        val response = doTest(Json.encodeToJsonElement(testUser))
        assertThat(response.resolvedException).isInstanceOf(MethodArgumentNotValidException::class.java)
        assertThat(response.resolvedException?.message).contains("Password must be 5 characters or more")
    }

    @Test
    fun `test POST register user already registered returns client error`() {
        val testUser = UserDTO("a@a.com", "12345")
        `when`(userService.registerUser(testUser.email, testUser.password))
            .thenThrow(RuntimeException())
        val response = doTest(Json.encodeToJsonElement(testUser))
        assertThat(response.resolvedException).isInstanceOf(RegistrationDeniedException::class.java)
        assertThat(response.response.contentAsString).contains("Registration is not permitted at this time")
    }

    private fun doTest(input: JsonElement): MvcResult {
        return mockMvc.post(url) {
            contentType = mType
            content = input
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()
    }
}