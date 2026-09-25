package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.User

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("subtrack_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && !getCurrentUserEmail().isNullOrBlank()
    }

    fun saveUserSession(user: User) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .apply()
    }

    fun getCurrentUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun getCurrentUserName(): String? {
        return prefs.getString(KEY_USER_NAME, "User")
    }

    fun getCurrentUserId(): Long {
        return prefs.getLong(KEY_USER_ID, 0L)
    }

    fun clearSession() {
        prefs.edit()
            .clear()
            .apply()
    }
}
