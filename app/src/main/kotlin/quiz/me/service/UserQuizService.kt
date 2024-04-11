package quiz.me.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import quiz.me.model.dao.UserEntity
import quiz.me.model.dto.ViewCompletedQuizDTO
import quiz.me.repository.UserQuizRepository

@Service
@Transactional
class UserQuizService(
    private val userQuizRepository: UserQuizRepository,
) {
    fun findAllByUser(user: UserEntity, pr: PageRequest): Page<ViewCompletedQuizDTO> =
        userQuizRepository.findAllByUser(user, pr.withSort(Sort.by("completedAt").descending()))
            .map { ViewCompletedQuizDTO(it.quiz.id, it.completedAt) }
}