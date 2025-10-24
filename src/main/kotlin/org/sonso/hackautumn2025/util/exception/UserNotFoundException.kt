package org.sonso.hackautumn2025.util.exception

class UserNotFoundException(message: String = "User not found") : AuthenticationException(message)
