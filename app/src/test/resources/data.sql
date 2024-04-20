insert into "user" ("id", "email", "password")
values
    ('be859744-ee6c-4c4e-87c8-3d6bcd600000', 'a@a.com', '$2a$12$4Zod/unWWBFi2lbPAP.LmuK3WqvDZQRcJw2zUCLzZBg15KIIEuAIe'),
    ('be859744-ee6c-4c4e-87c8-3d6bcd600001', 'b@a.com', '$2a$12$IvpLrSwQGPlgX1hIgOJ29OKiHMBM1kFDSzItSIhaJyeu.3ImhdOHO'),
    ('be859744-ee6c-4c4e-87c8-3d6bcd600002', 'noquizzes@a.com', '$2a$12$j.E2EEBtLEhI1NoLmCFfHOqwv5K5u7ft8drpMxGVWHwqLTCupD.La');

insert into "user_authorities" ("user_id", "authorities")
values
    ('be859744-ee6c-4c4e-87c8-3d6bcd600000', 'ROLE_USER'),
    ('be859744-ee6c-4c4e-87c8-3d6bcd600001', 'ROLE_USER'),
    ('be859744-ee6c-4c4e-87c8-3d6bcd600002', 'ROLE_ADMIN');

insert into "quiz" ("id", "title", "text", "author_user_id")
values
    (1, 'Quiz 1 (ans 1 usr 1)', 'Test quiz one answer', 'be859744-ee6c-4c4e-87c8-3d6bcd600000'),
    (2, 'Quiz 2 (ans 2,3 usr 2)', 'Test quiz two answer', 'be859744-ee6c-4c4e-87c8-3d6bcd600001'),
    (3, 'Quiz 3 (ans [] usr 1)', 'Test quiz no answer unsolved', 'be859744-ee6c-4c4e-87c8-3d6bcd600000');

insert into "quiz_options" ("quiz_id", "option")
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

insert into "quiz_answers" ("quiz_id", "answer")
values
    (1, 1),
    (2, 2),
    (2, 3);

insert into "user_quiz" ("quiz_id", "user_id", "completed_at")
values
    (1, 'be859744-ee6c-4c4e-87c8-3d6bcd600000', '2024-04-01 10:00:00.000000'),
    (1, 'be859744-ee6c-4c4e-87c8-3d6bcd600001', '2024-04-02 10:00:00.000000'),
    (2, 'be859744-ee6c-4c4e-87c8-3d6bcd600001', '2024-04-03 10:00:00.000000'),
    (1, 'be859744-ee6c-4c4e-87c8-3d6bcd600000', '2024-04-04 10:00:00.000000');
