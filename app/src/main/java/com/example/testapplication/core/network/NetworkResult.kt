package com.example.testapplication.core.network

/**
 * Sealed class to model the three states of any network operation.
 * Used by Repositories to communicate results back to Presenters.
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int = -1) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}
