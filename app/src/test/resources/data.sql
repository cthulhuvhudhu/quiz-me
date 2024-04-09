insert into USER (ID, EMAIL, PASSWORD, AUTHORITY) values ('be859744-ee6c-4c4e-87c8-3d6bcd600000', 'a@a.com', '$2a$12$jh3a4lM4EVNmQ5BYJb12dOyiuKgmKjW7UtPHAKOEV6wPfEyzf.G6S', 'ROLE_USER');

insert into QUIZ (ID, TEXT, TITLE, AUTHOR_USER_ID) values (16, 'Select option 1', 'Quiz 15', 'be859744-ee6c-4c4e-87c8-3d6bcd600000');

insert into QUIZ_OPTION (QUIZ_ID, OPTION) values (16, '1');
insert into QUIZ_OPTION (QUIZ_ID, OPTION) values (16, '2');
insert into QUIZ_OPTION (QUIZ_ID, OPTION) values (16, '3');
insert into QUIZ_OPTION (QUIZ_ID, OPTION) values (16, '4');

insert into QUIZ_ANSWER (QUIZ_ID, ANSWER) values (16, 1);

insert into USER_QUIZ (QUIZ_ID, USER_ID, COMPLETED_AT) values (16, 'be859744-ee6c-4c4e-87c8-3d6bcd600000', '2024-04-04 10:00:00.000');