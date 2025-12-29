package com.mentorme.app.data.session

object SessionManager {
    private var token: String? = null

    fun setToken(t: String?) {
        token = t
    }

    fun getToken(): String? = token

    fun clear() {
        token = null
    }
}
