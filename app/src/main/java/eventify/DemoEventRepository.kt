package eventify

import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DemoEventRepository(
    initialEvents: List<EventDetails>
) : EventRepository {

    private val localState = MutableStateFlow<EventsSnapshot?>(
        EventsSnapshot(
            events = initialEvents,
            lastSyncedAt = null,
            hasPendingWrites = false
        )
    )

    override fun observeEvents(): Flow<EventsSnapshot?> {
        return localState.asStateFlow()
    }

    override suspend fun refreshEvents() {
        // Simulate a short refresh operation
        delay(700)

        localState.value = localState.value?.copy(
            lastSyncedAt = Instant.now()
        )
    }
}
