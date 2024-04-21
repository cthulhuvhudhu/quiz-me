DROP TABLE IF EXISTS "user" CASCADE;
create table "user"
(
    "id"       CHARACTER VARYING(255) not null
        primary key,
    "email"    CHARACTER VARYING(255) not null
        constraint USER_EMAIL_UNIQUE
            unique,
    "password" CHARACTER VARYING(255) not null
);

DROP TABLE IF EXISTS "user_authorities";
create table "user_authorities"
(
    "user_id"     CHARACTER VARYING(255) not null,
    "authorities" CHARACTER VARYING(255),
    constraint USER_AUTHORITIES_FK
        foreign key ("user_id") references "user"
);

DROP TABLE IF EXISTS "quiz" CASCADE;
create table "quiz"
(
    "id"             BIGINT auto_increment
        primary key,
    "text"           CHARACTER VARYING(255),
    "title"          CHARACTER VARYING(255),
    "author_user_id" CHARACTER VARYING(255),
    constraint QUIZ_USER_FK
        foreign key ("author_user_id") references "user"
);

DROP TABLE IF EXISTS "user_quiz";
create table "user_quiz"
(
    "id"             BIGINT auto_increment
        primary key,
    "completed_at" TIMESTAMP              DEFAULT NOW(),
    "quiz_id"      BIGINT                 not null,
    "user_id"      CHARACTER VARYING(255) not null,
    constraint USER_QUIZ_USER_FK
        foreign key ("quiz_id") references "quiz",
    constraint USER_QUIZ_QUIZ_FK
        foreign key ("user_id") references "user"
);

DROP TABLE IF EXISTS "quiz_answers";
create table "quiz_answers"
(
    "quiz_id" BIGINT not null,
    "answer"  INTEGER,
    constraint QUIZ_ANSWER_QUIZ_FK
        foreign key ("quiz_id") references "quiz"
);

DROP TABLE IF EXISTS "quiz_options";
create table "quiz_options"
(
    "quiz_id" BIGINT not null,
    "option"  CHARACTER VARYING(255),
    constraint QUIZ_OPTION_QUIZ_FK
        foreign key ("quiz_id") references "quiz"
);
