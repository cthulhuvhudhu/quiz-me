package quiz.me

class OwnershipPermissionDeniedException : RuntimeException("You do not have permission to delete the quiz")
class QuizNotFoundException(id: Long) : RuntimeException("Unable to find quiz '$id'")
class RegistrationDeniedException : RuntimeException("Registration is not permitted at this time. Please try again later or with different credentials.")
