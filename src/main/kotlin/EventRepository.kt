package eventify

import kotlinx.coroutines.flow.Flow

interface EventRepository {
    // Emits null only when this account has no initialized local snapshot.
    // Emits immutable snapshots after local transactions, including sync.
    fun observeEvents(): Flow<EventsSnapshot?>

    // Main-safe. Reconciles server data into local storage; does not return
    // a second, competing list directly to the ViewModel.
    // Must persist an initialized snapshot for a successful empty response.
    // Expected transport/auth failures are mapped by the implementation.
    suspend fun refreshEvents()
}
