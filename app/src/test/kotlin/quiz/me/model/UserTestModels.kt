package quiz.me.model

import quiz.me.model.dao.UserEntity
import quiz.me.model.dto.UserDTO

object UserTestModels {
    val dneUser = UserEntity(
        id = "be859744-0000-4c4e-87c8-3d6bcd611111",
        email = "DNE@a.com",
        password = "fakePassword",
        authority = "ROLE_ADMIN"
    )

    val users = listOf(
        UserSet("be859744-ee6c-4c4e-87c8-3d6bcd600000", "a@a.com"),
        UserSet("be859744-ee6c-4c4e-87c8-3d6bcd600001", "b@a.com"),
        UserSet("be859744-ee6c-4c4e-87c8-3d6bcd600002", "noquizzes@a.com"),
    )
}

class UserSet(
    id: String,
    val email: String
) {
    val entityIn: UserEntity = UserEntity(email = email, password = "encrypted${id.takeLast(4)}")
    val entityOut: UserEntity = UserEntity(id, email, "encrypted${id.takeLast(4)}")
    val dto: UserDTO = UserDTO(email, "password${id.takeLast(4)}")
}