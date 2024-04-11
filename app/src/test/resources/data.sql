insert into USER (ID, EMAIL, PASSWORD, AUTHORITY)
values
    ('be859744-ee6c-4c4e-87c8-3d6bcd600000', 'a@a.com', 'encrypted0000', 'ROLE_USER'),
    ('be859744-ee6c-4c4e-87c8-3d6bcd600001', 'b@a.com', 'encrypted0001', 'ROLE_USER'),
    ('be859744-ee6c-4c4e-87c8-3d6bcd600002', 'noquizzes@a.com', 'encrypted0002', 'ROLE_USER');

insert into QUIZ (ID, TITLE, TEXT, AUTHOR_USER_ID)
values
    (1, 'Quiz 1 (ans 1 usr 1)', 'Test quiz one answer', 'be859744-ee6c-4c4e-87c8-3d6bcd600000'),
    (2, 'Quiz 2 (ans 2,3 usr 2)', 'Test quiz two answer', 'be859744-ee6c-4c4e-87c8-3d6bcd600001'),
    (3, 'Quiz 3 (ans [] usr 1)', 'Test quiz no answer unsolved', 'be859744-ee6c-4c4e-87c8-3d6bcd600000');

insert into QUIZ_OPTION (QUIZ_ID, OPTION)
values
    (1, '1'),
    (1, '2'),
    (1, '3'),
    (1, '4'),
    (2, '1'),
    (2, '2'),
    (2, '3'),
    (2, '4'),
    (3, '1'),
    (3, '2'),
    (3, '3'),
    (3, '4');

insert into QUIZ_ANSWER (QUIZ_ID, ANSWER)
values
    (1, 1),
    (2, 2),
    (2, 3);

insert into USER_QUIZ (QUIZ_ID, USER_ID, COMPLETED_AT)
values
    (1, 'be859744-ee6c-4c4e-87c8-3d6bcd600000', '2024-04-01 10:00:00.000000'),
    (1, 'be859744-ee6c-4c4e-87c8-3d6bcd600001', '2024-04-02 10:00:00.000000'),
    (2, 'be859744-ee6c-4c4e-87c8-3d6bcd600001', '2024-04-03 10:00:00.000000'),
    (1, 'be859744-ee6c-4c4e-87c8-3d6bcd600000', '2024-04-04 10:00:00.000000');
