# quiz-me
Kotlin Spring API for serving quizzes. 
This follows (and goes beyond) the project guidelines by HyperSkill/JetBrains for Kotlin for a popular advanced coding puzzle.

## Project Description

This project is a Kotlin Spring API for serving quizzes. It is a RESTful API that allows users to create, read, update, 
delete, and solve quizzes. It also allows users to solve quizzes and get feedback on their answers. 
The API is secured using Basic Auth and Spring Security.

## Technologies Used

- Kotlin
- Spring Boot
- Spring Security
- Spring Data JPA
- H2 Database
- Gradle
- JUnit
- MockMvc
- TestContainers

## Features

- Create a quiz - It supports multiple choice, true/false, and "none" answer types.
- Read a quiz
- Update a quiz
- Delete a quiz - Only quiz creators can delete a quiz.
- Solve a quiz
- Register a user
- View quizzes
- View solved quizzes
