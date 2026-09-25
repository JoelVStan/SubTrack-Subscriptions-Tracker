package com.example.data

import com.example.model.User
import com.example.util.SecurityUtils

class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {

    suspend fun hasAnyRegisteredUser(): Boolean {
        return userDao.getUserCount() > 0
    }

    suspend fun signUp(name: String, email: String, password: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your name"))
        }
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters long"))
        }

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists. Please log in."))
        }

        val passwordHash = SecurityUtils.hashPassword(password)
        val newUser = User(
            name = trimmedName,
            email = trimmedEmail,
            passwordHash = passwordHash
        )

        val newId = userDao.insertUser(newUser)
        val createdUser = newUser.copy(id = newId)
        sessionManager.saveUserSession(createdUser)
        return Result.success(createdUser)
    }

    suspend fun login(email: String, password: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()

        if (trimmedEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your email"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your password"))
        }

        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(IllegalArgumentException("Account not found. Please sign up."))

        val isValid = SecurityUtils.verifyPassword(password, user.passwordHash)
        if (!isValid) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        sessionManager.saveUserSession(user)
        return Result.success(user)
    }

    suspend fun getCurrentUser(): User? {
        val email = sessionManager.getCurrentUserEmail() ?: return null
        return userDao.getUserByEmail(email)
    }

    fun isUserLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
