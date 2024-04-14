package quiz.me.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import quiz.me.model.dao.UserQuizEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserQuizRepository : JpaRepository<UserQuizEntity, Long> {
    fun findAllByUser_Email(email: String, page: PageRequest): Page<UserQuizEntity>
}