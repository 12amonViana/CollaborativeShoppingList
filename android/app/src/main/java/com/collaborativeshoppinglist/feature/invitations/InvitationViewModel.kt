package com.collaborativeshoppinglist.feature.invitations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collaborativeshoppinglist.core.error.AppErrorMapper
import com.collaborativeshoppinglist.data.model.Invitation
import com.collaborativeshoppinglist.data.repository.InvitationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvitationUiState(
    val invitations: List<Invitation> = emptyList(),
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val repository: InvitationRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(InvitationUiState())
    val state: StateFlow<InvitationUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                repository.observePendingInvitations().collect { invitations ->
                    _state.update {
                        it.copy(invitations = invitations, isLoading = false, error = null)
                    }
                }
            }.onFailure(::showError)
        }
    }

    fun create(listId: String, email: String) = perform("Convite enviado.") {
        repository.create(listId, email)
    }

    fun accept(invitationId: String, onAccepted: (String) -> Unit) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true, error = null) }
            runCatching { repository.accept(invitationId) }
                .onSuccess { listId ->
                    _state.update { it.copy(isWorking = false, message = "Convite aceito.") }
                    onAccepted(listId)
                }
                .onFailure(::showError)
        }
    }

    fun clearMessages() = _state.update { it.copy(message = null, error = null) }

    private fun perform(successMessage: String, action: suspend () -> Unit) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true, error = null, message = null) }
            runCatching { action() }
                .onSuccess {
                    _state.update { it.copy(isWorking = false, message = successMessage) }
                }
                .onFailure(::showError)
        }
    }

    private fun showError(error: Throwable) {
        _state.update {
            it.copy(
                isLoading = false,
                isWorking = false,
                error = AppErrorMapper.from(error).message,
            )
        }
    }
}
