DROP TABLE IF EXISTS PUBLIC.USER CASCADE;
create table PUBLIC.USER
(
    ID        CHARACTER VARYING(255) not null
        primary key,
    EMAIL     CHARACTER VARYING(255) not null
        constraint USER_EMAIL_UNIQUE
            unique,
    PASSWORD  CHARACTER VARYING(255) not null,
    AUTHORITY CHARACTER VARYING(255)
);


DROP TABLE IF EXISTS PUBLIC.QUIZ CASCADE;
create table PUBLIC.QUIZ
(
    ID             BIGINT auto_increment
        primary key,
    TEXT           CHARACTER VARYING(255) not null,
    TITLE          CHARACTER VARYING(255) not null,
    AUTHOR_USER_ID CHARACTER VARYING(255) not null,
    constraint QUIZ_USER_FK
        foreign key (AUTHOR_USER_ID) references PUBLIC.USER
);

DROP TABLE IF EXISTS PUBLIC.USER_QUIZ;
create table PUBLIC.USER_QUIZ
(
    QUIZ_ID      BIGINT                 not null,
    USER_ID      CHARACTER VARYING(255) not null,
    COMPLETED_AT TIMESTAMP,
    primary key (QUIZ_ID, USER_ID, COMPLETED_AT),
    constraint USER_QUIZ_USER_FK
        foreign key (USER_ID) references PUBLIC.USER,
    constraint USER_QUIZ_QUIZ_FK
        foreign key (QUIZ_ID) references PUBLIC.QUIZ
);

DROP TABLE IF EXISTS PUBLIC.QUIZ_ANSWER;
create table PUBLIC.QUIZ_ANSWER
(
    QUIZ_ID    BIGINT  not null,
    ANSWER    INTEGER not null,
    constraint QUIZ_ANSWER_QUIZ_FK
        foreign key (QUIZ_ID) references PUBLIC.QUIZ
);

DROP TABLE IF EXISTS PUBLIC.QUIZ_OPTION;
create table PUBLIC.QUIZ_OPTION
(
    QUIZ_ID    BIGINT                 not null,
    OPTION     CHARACTER VARYING(255) not null,
    constraint QUIZ_OPTION_QUIZ_FK
        foreign key (QUIZ_ID) references PUBLIC.QUIZ
);
