insert into USER (ID, EMAIL, PASSWORD, AUTHORITY)
values
    ('be859744-ee6c-4c4e-87c8-3d6bcd600000', 'a@a.com', 'password', 'ROLE_USER'),
    ('be859744-ee6c-4c4e-87c8-3d6bcd600001', 'b@a.com', 'password', 'ROLE_USER');

insert into QUIZ (ID, TEXT, TITLE, AUTHOR_USER_ID)
values
    (1, 'Select option 1', 'Quiz 1', 'be859744-ee6c-4c4e-87c8-3d6bcd600000'),
    (2, 'Select option 1 & 2', 'Quiz 2', 'be859744-ee6c-4c4e-87c8-3d6bcd600001');

insert into QUIZ_OPTION (QUIZ_ID, OPTION)
values
    (1, '1'),
    (1, '2'),
    (1, '3'),
    (1, '4'),
    (2, '1'),
    (2, '2'),
    (2, '3'),
    (2, '4');

insert into QUIZ_ANSWER (QUIZ_ID, ANSWER)
values
    (1, 1),
    (2, 1),
    (2, 2);

insert into USER_QUIZ (QUIZ_ID, USER_ID, COMPLETED_AT)
values
    (1, 'be859744-ee6c-4c4e-87c8-3d6bcd600000', '2024-04-04 10:00:00.000'),
    (2, 'be859744-ee6c-4c4e-87c8-3d6bcd600001', '2024-04-04 11:00:00.000'),
    (1, 'be859744-ee6c-4c4e-87c8-3d6bcd600001', '2024-04-04 12:00:00.000');