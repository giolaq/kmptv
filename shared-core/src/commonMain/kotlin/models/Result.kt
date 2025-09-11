package com.kmptv.shared_core.models

/**
 * Result wrapper for handling success/error states
 */
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception, val message: String) : Result<T>()
    data class Loading<T>(val progress: Float? = null) : Result<T>()
    
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
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

/**
 * User credentials for authentication
 */
data class UserCredentials(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val oauthToken: String? = null,
    val biometricToken: String? = null
) {
    fun isValid(): Boolean {
        return (username != null || email != null) && 
               (password != null || oauthToken != null || biometricToken != null)
    }
}