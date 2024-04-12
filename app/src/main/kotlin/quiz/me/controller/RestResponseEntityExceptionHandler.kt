package quiz.me.controller

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import quiz.me.OwnershipPermissionDeniedException
import quiz.me.QuizNotFoundException
import quiz.me.RegistrationDeniedException
import java.time.LocalDateTime

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(
        ConstraintViolationException::class,
        RegistrationDeniedException::class
    )
    protected fun handleBadRequest(e: RuntimeException, request: WebRequest): ResponseEntity<Any> =
        customErrorResponse(request, listOfNotNull(e.message))

    @ExceptionHandler(EntityNotFoundException::class, QuizNotFoundException::class)
    protected  fun handleNotFound(e: RuntimeException, request: WebRequest): ResponseEntity<Any> =
        customErrorResponse(request, listOfNotNull(e.message), HttpStatus.NOT_FOUND)

    @ExceptionHandler(OwnershipPermissionDeniedException::class)
    protected fun handlePermissionDenied(e: RuntimeException, request: WebRequest): ResponseEntity<Any> =
        customErrorResponse(request, listOfNotNull(e.message), HttpStatus.FORBIDDEN)

    @ExceptionHandler(UsernameNotFoundException::class)
    protected fun handleUnauthorized(e: RuntimeException, request: WebRequest): ResponseEntity<Any> =
        customErrorResponse(request, listOfNotNull(e.message), HttpStatus.UNAUTHORIZED)

    override fun handleHttpMessageNotReadable(
        e: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? = customErrorResponse(request, listOfNotNull(e.message))

    override fun handleMethodArgumentNotValid(
        e: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? = e
        .bindingResult
        .fieldErrors
        .mapNotNull { fieldError -> fieldError.defaultMessage }.let {
            customErrorResponse(request, it)
        }

    private fun customErrorResponse(request: WebRequest, errors: List<String>, status: HttpStatus = HttpStatus.BAD_REQUEST): ResponseEntity<Any> {
        val body = CustomErrorMessage(
            status.value(),
            LocalDateTime.now(),
            request.getDescription(false),
            errors
        )
        return ResponseEntity(body, status)
    }
}

data class CustomErrorMessage (
    val statusCode: Int,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val description: String,
    val errors: List<String> = emptyList()
)
