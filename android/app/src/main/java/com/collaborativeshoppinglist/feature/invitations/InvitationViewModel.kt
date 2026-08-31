package com.collaborativeshoppinglist.feature.invitations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collaborativeshoppinglist.core.error.AppErrorMapper
import com.collaborativeshoppinglist.data.repository.InvitationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvitationUiState(
    val invitationCode: String? = null,
    val isWorking: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val repository: InvitationRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(InvitationUiState())
    val state: StateFlow<InvitationUiState> = _state.asStateFlow()

    fun create(listId: String) = perform { code ->
        repository.create(listId).also(code)
    }

    fun accept(code: String, onAccepted: (String) -> Unit) = perform(onAccepted) {
        repository.accept(code)
    }

    fun clearError() = _state.update { it.copy(error = null) }

    private fun perform(onSuccess: (String) -> Unit = {}, action: suspend ((String) -> Unit) -> String) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true, error = null) }
            runCatching { action { code -> _state.update { it.copy(invitationCode = code) } } }
                .onSuccess { result ->
                    _state.update { it.copy(isWorking = false) }
                    onSuccess(result)
                }
                .onFailure(::showError)
        }
    }

    private fun showError(error: Throwable) {
        val message = when (error.message) {
            "INVITATION_EXPIRED" -> "Este convite expirou."
            "INVITATION_UNAVAILABLE" -> "Convite inválido, já utilizado ou indisponível."
            "LIST_CLOSED" -> "A lista já foi encerrada."
            "NOT_AUTHORIZED" -> "Você não tem permissão para esta ação."
            else -> AppErrorMapper.from(error).message
        }
        _state.update { it.copy(isWorking = false, error = message) }
    }
}
