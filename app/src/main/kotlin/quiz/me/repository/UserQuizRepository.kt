package quiz.me.repository

import quiz.me.model.dao.UserQuizEntity
import quiz.me.model.dao.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface UserQuizRepository : JpaRepository<UserQuizEntity, Long> {
    fun findAllByUser(user: UserEntity, page: Pageable): Page<UserQuizEntity>
}