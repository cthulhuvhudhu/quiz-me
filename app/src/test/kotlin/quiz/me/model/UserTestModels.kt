package quiz.me.model

import quiz.me.model.dao.UserEntity

object UserTestModels {
    val dneUser = UserEntity(
        id = "be859744-0000-4c4e-87c8-3d6bcd611111",
        email = "DNE@a.com",
        password = "fakePassword",
        authority = "ROLE_ADMIN"
    )

    val users = listOf(
        UserEntity(
            id = "be859744-ee6c-4c4e-87c8-3d6bcd600000",
            email = "a@a.com",
            password = "password",
            authority = "ROLE_USER"
        ),
        UserEntity(
            id = "be859744-ee6c-4c4e-87c8-3d6bcd600001",
            email = "b@a.com",
            password = "password",
            authority = "ROLE_USER"
        ),
        UserEntity(
            id = "be859744-ee6c-4c4e-87c8-3d6bcd600002",
            email = "noquizzes@a.com",
            password = "password",
            authority = "ROLE_USER"
        )
    )
}