package com.kmptv.shared_core.models

sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception, val message: String) : Result<T>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (Exception) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }
}

data class UserCredentials(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val oauthToken: String? = null,
    val biometricToken: String? = null,
) {
    fun isValid(): Boolean =
        (username != null || email != null) &&
            (password != null || oauthToken != null || biometricToken != null)
}
