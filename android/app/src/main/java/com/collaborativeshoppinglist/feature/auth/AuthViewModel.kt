package com.collaborativeshoppinglist.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collaborativeshoppinglist.core.error.AppErrorMapper
import com.collaborativeshoppinglist.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                _state.update { it.copy(isAuthenticated = user != null, isLoading = false) }
            }
        }
    }

    fun signIn(email: String, password: String) =
        submit { repository.signIn(email, password) }

    fun register(name: String, email: String, password: String) =
        submit { repository.register(name, email, password) }

    fun signOut() = repository.signOut()

    fun clearError() = _state.update { it.copy(error = null) }

    private fun submit(action: suspend () -> Unit) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { action() }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, error = AppErrorMapper.from(error).message)
                    }
                }
        }
    }
}
