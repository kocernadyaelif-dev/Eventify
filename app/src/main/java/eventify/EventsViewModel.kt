package eventify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface EventsUiState {
    object Loading : EventsUiState
    data class Success(
        val events: List<EventDetails>,
        val refreshing: Boolean,
        val refreshError: String?,
        val lastSyncedAt: Instant?,
        val hasPendingWrites: Boolean
    ) : EventsUiState
    data class Error(val message: String) : EventsUiState
}

private sealed interface LocalRead {
    object Loading : LocalRead
    data class Data(val snapshot: EventsSnapshot?) : LocalRead
    object Failed : LocalRead
}

private data class Refresh(
    val running: Boolean = false,
    val error: String? = null
)

class EventsViewModel(
    private val repository: EventRepository
) : ViewModel() {
    private val local = MutableStateFlow<LocalRead>(LocalRead.Loading)
    private val refresh = MutableStateFlow(Refresh())
    private var observationJob: Job? = null
    private var refreshJob: Job? = null

    // Eager for the lifetime of this screen-scoped ViewModel. The repository
    // owns network subscriptions; collecting this local flow opens no socket.
    val uiState: StateFlow<EventsUiState> = combine(local, refresh) { read, sync ->
        when (read) {
            LocalRead.Loading -> EventsUiState.Loading
            LocalRead.Failed -> EventsUiState.Error("Cannot read saved events.")
            is LocalRead.Data -> {
                val snapshot = read.snapshot
                when {
                    snapshot != null -> EventsUiState.Success(
                        snapshot.events, sync.running, sync.error,
                        snapshot.lastSyncedAt, snapshot.hasPendingWrites
                    )
                    sync.error != null -> EventsUiState.Error(sync.error)
                    else -> EventsUiState.Loading
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EventsUiState.Loading)

    init {
        observeLocalEvents()
        loadEvents()
    }

    private fun observeLocalEvents() {
        observationJob = viewModelScope.launch {
            local.value = LocalRead.Loading
            try {
                repository.observeEvents().collect { snapshot ->
                    local.value = LocalRead.Data(snapshot)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                local.value = LocalRead.Failed
            }
        }
    }

    // Call on the main thread from a refresh or retry UI action.
    fun loadEvents() {
        if (refreshJob?.isActive == true) return
        if (observationJob?.isActive != true) observeLocalEvents()
        refreshJob = viewModelScope.launch {
            refresh.value = Refresh(running = true)
            try {
                repository.refreshEvents()
                refresh.value = Refresh()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                refresh.value = Refresh(
                    error = "Refresh failed. Check your connection and retry."
                )
            }
        }
    }
}
