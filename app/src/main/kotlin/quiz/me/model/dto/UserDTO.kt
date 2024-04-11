package quiz.me.model.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class UserDTO (
    @field:Email(message = "Email is not valid", regexp = "^[\\w-\\._\\+]+@\\w+[.][\\w-]{2,4}$")
    @field:NotEmpty(message = "Email cannot be empty")
    val email: String,
    @field:Size(min=5, message = "Password must be 5 characters or more")
    val password: String
)
