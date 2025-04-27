package com.example.cahut.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cahut.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterState(
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val error: String? = null
)

class RegisterViewModel : ViewModel() {
    private var authRepository: AuthRepository? = null
    private val _registerState = MutableStateFlow(RegisterState())
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun initialize(context: Context) {
        authRepository = AuthRepository(context)
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _registerState.value = _registerState.value.copy(isLoading = true, error = null)
                authRepository?.register(username, email, password)
                _registerState.value = _registerState.value.copy(isLoading = false, isRegistered = true)
            } catch (e: Exception) {
                _registerState.value = _registerState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Đăng ký thất bại"
                )
            }
        }
    }

    fun clearError() {
        _registerState.value = _registerState.value.copy(error = null)
    }
} 