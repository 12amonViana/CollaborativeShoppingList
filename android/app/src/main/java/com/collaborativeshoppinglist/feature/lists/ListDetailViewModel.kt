package com.collaborativeshoppinglist.feature.lists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collaborativeshoppinglist.core.error.AppErrorMapper
import com.collaborativeshoppinglist.data.model.Membership
import com.collaborativeshoppinglist.data.model.ShoppingList
import com.collaborativeshoppinglist.data.model.ShoppingListItem
import com.collaborativeshoppinglist.data.model.ShoppingListStatus
import com.collaborativeshoppinglist.data.repository.AuthRepository
import com.collaborativeshoppinglist.data.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListDetailUiState(
    val list: ShoppingList? = null,
    val items: List<ShoppingListItem> = emptyList(),
    val members: List<Membership> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val error: String? = null,
) {
    val isClosed: Boolean get() = list?.status == ShoppingListStatus.CLOSED
    val isOwner: Boolean get() = list?.ownerId == currentUserId
}

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ShoppingListRepository,
    authRepository: AuthRepository,
) : ViewModel() {
    private val listId: String = requireNotNull(savedStateHandle["listId"])
    private var observationJob: Job? = null
    private val _state = MutableStateFlow(
        ListDetailUiState(currentUserId = authRepository.currentUserId.orEmpty()),
    )
    val state: StateFlow<ListDetailUiState> = _state.asStateFlow()

    init {
        observe()
    }

    fun retry() = observe()
    fun addItem(name: String) = perform { repository.addItem(listId, name) }
    fun updateQuantity(itemId: String, quantity: Int) =
        perform { repository.updateQuantity(listId, itemId, quantity) }
    fun removeItem(itemId: String) = perform { repository.removeItem(listId, itemId) }
    fun setCartStatus(itemId: String, inCart: Boolean) =
        perform { repository.setCartStatus(listId, itemId, inCart) }
    fun closeList() = perform { repository.closeList(listId) }

    private fun observe() {
        observationJob?.cancel()
        _state.update { it.copy(isLoading = true, error = null) }
        observationJob = viewModelScope.launch {
            launch {
                repository.observeList(listId).catch { showError(it) }.collect { value ->
                    _state.update { it.copy(list = value, isLoading = false) }
                }
            }
            launch {
                repository.observeItems(listId).catch { showError(it) }.collect { value ->
                    _state.update { it.copy(items = value, isLoading = false) }
                }
            }
            launch {
                repository.observeMembers(listId).catch { showError(it) }.collect { value ->
                    _state.update { it.copy(members = value) }
                }
            }
        }
    }

    private fun perform(action: suspend () -> Unit) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true, error = null) }
            runCatching { action() }
                .onSuccess { _state.update { it.copy(isWorking = false) } }
                .onFailure(::showError)
        }
    }

    private fun showError(error: Throwable) {
        val message = if (error.message == "LIST_CLOSED") "A lista já foi encerrada."
        else AppErrorMapper.from(error).message
        _state.update { it.copy(isLoading = false, isWorking = false, error = message) }
    }
}
