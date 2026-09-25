package com.example.util

import java.security.MessageDigest

object SecurityUtils {
    private const val SALT = "subtrack_secure_salt_2026_finance"

    fun hashPassword(password: String): String {
        val input = password + SALT
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return hashPassword(password) == storedHash
    }
}
