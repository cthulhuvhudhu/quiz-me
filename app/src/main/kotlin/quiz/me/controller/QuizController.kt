package quiz.me.controller

import quiz.me.model.dao.UserEntity
import quiz.me.model.dto.*
import quiz.me.service.QuizService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import quiz.me.service.UserQuizService

@RestController
@RequestMapping(value = ["api/quizzes"])
class QuizController (
    private val quizService: QuizService,
    private val userQuizService: UserQuizService
) {

    @GetMapping
    fun getQuizzes(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = DEFAULT_PAGE_SIZE) size: Int
    ): ResponseEntity<Page<QuizDTO>> =
        quizService.getQuizzes(PageRequest.of(page, size)).let{
            return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(it)
        }

    @GetMapping("/{id}")
    fun getQuiz(
        @PathVariable id: Long
    ): ResponseEntity<QuizDTO?> =
        quizService.getQuiz(id)?.let {
            return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(it)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found for id = $id")

    @GetMapping("/completed", params = ["page"])
    fun getCompletedQuizzes(
        @AuthenticationPrincipal user: UserDetails,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = DEFAULT_PAGE_SIZE) size: Int
    ): ResponseEntity<Page<ViewCompletedQuizDTO>> =
        userQuizService.findAllByUserEmail(user.username, PageRequest.of(page, size)).let {
            ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(it)
    }

    @PostMapping
    fun createQuiz(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody @Valid quizDTO: CreateQuizDTO
    ): ResponseEntity<QuizDTO> =
        quizService.addQuiz(quizDTO, UserEntity(email = user.username)).let {
            return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(it)
        }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteQuiz(
//        @AuthenticationPrincipal user: UserEntity,
        @PathVariable id: Long
    ) = quizService.deleteQuiz(id)//, user) // TODO

    @PostMapping("/{id}/solve")
    fun checkAnswer(
//        @AuthenticationPrincipal user: UserEntity,
        @PathVariable id: Long,
        @RequestBody guess: GuessDTO
    ): ResponseEntity<FeedbackDTO?> =
        quizService.gradeQuiz(id, guess.answer)?.let { //, user)?.let {
            return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(it)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found for id = $id")

    companion object {
        private const val DEFAULT_PAGE_SIZE = "10"
    }
}
