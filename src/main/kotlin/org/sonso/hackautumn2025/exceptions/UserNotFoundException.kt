package org.sonso.hackautumn2025.exceptions

class UserNotFoundException(message: String = "User not found") : AuthenticationException(message)
