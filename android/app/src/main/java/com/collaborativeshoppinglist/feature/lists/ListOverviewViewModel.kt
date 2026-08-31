package com.collaborativeshoppinglist.feature.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collaborativeshoppinglist.core.error.AppErrorMapper
import com.collaborativeshoppinglist.data.model.ShoppingList
import com.collaborativeshoppinglist.data.repository.AuthRepository
import com.collaborativeshoppinglist.data.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListOverviewUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ListOverviewViewModel @Inject constructor(
    private val repository: ShoppingListRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ListOverviewUiState())
    val state: StateFlow<ListOverviewUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                repository.observeLists().collect { lists ->
                    _state.update { it.copy(lists = lists, isLoading = false, error = null) }
                }
            }.onFailure(::showError)
        }
    }

    fun createList(name: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isCreating = true, error = null) }
            runCatching { repository.createList(name) }
                .onSuccess { id ->
                    _state.update { it.copy(isCreating = false) }
                    onCreated(id)
                }
                .onFailure(::showError)
        }
    }

    fun signOut() = authRepository.signOut()

    private fun showError(error: Throwable) {
        _state.update {
            it.copy(isLoading = false, isCreating = false, error = AppErrorMapper.from(error).message)
        }
    }
}
