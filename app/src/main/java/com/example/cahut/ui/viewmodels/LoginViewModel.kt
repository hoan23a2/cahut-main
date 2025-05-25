package com.example.cahut.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cahut.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    private var authRepository: AuthRepository? = null
    
    fun initialize(context: Context) {
        authRepository = AuthRepository(context)
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, error = null)
            
            authRepository?.login(email, password)?.fold(
                onSuccess = {
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                },
                onFailure = { exception ->
                    Log.e("LoginViewModel", "Login failed", exception)
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Đăng nhập thất bại"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _loginState.value = _loginState.value.copy(error = null)
    }
} 