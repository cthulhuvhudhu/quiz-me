package quiz.me.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import quiz.me.RegistrationDeniedException
import quiz.me.model.dto.UserDTO
import quiz.me.service.UserService

@RestController
@RequestMapping(value = ["api/register"])
class RegistrationController(
    private val userService: UserService
) {
    @PostMapping
    fun registerUser(@RequestBody @Valid registration: UserDTO) {
        try {
            userService.registerUser(registration.email, registration.password)
        } catch (e: Exception) {
            throw RegistrationDeniedException()
        }
    }
}
