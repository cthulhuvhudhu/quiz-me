package quiz.me.model

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import quiz.me.model.dao.UserEntity
import quiz.me.model.dto.UserDTO

object UserTestModels {
    val dneUser = UserEntity(
        id = "be859744-0000-4c4e-87c8-3d6bcd611111",
        email = "DNE@a.com",
        password = "fakePassword",
        authorities = mutableListOf("ROLE_ADMIN")
    )

    val users = listOf(
        UserSet("be859744-ee6c-4c4e-87c8-3d6bcd600000", "a@a.com"),
        UserSet("be859744-ee6c-4c4e-87c8-3d6bcd600001", "b@a.com"),
        UserSet("be859744-ee6c-4c4e-87c8-3d6bcd600002", "noquizzes@a.com"),
    )
}

val passwordEncoder = BCryptPasswordEncoder(12)

class UserSet(
    id: String,
    val email: String
) {
    val entityIn: UserEntity = UserEntity(email = email, password = passwordEncoder.encode("password${id.takeLast(4)}"))
    val entityOut: UserEntity = UserEntity(id, email, password = passwordEncoder.encode("password${id.takeLast(4)}"), authorities = listOf("ROLE_USER"))
    val dto: UserDTO = UserDTO(email, "password${id.takeLast(4)}")
}