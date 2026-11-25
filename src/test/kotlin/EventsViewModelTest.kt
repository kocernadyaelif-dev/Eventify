package eventify

import androidx.lifecycle.ViewModelStore
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class FakeEventRepository : EventRepository {
    val snapshots = MutableStateFlow<EventsSnapshot?>(null)
    var refreshAction: suspend () -> Unit = {}
    var refreshCalls = 0
    var activeObservers = 0
    override fun observeEvents(): Flow<EventsSnapshot?> = flow {
        activeObservers++
        try { emitAll(snapshots) } finally { activeObservers-- }
    }
    override suspend fun refreshEvents() {
        refreshCalls++
        refreshAction()
    }
}

private fun sample(title: String): EventsSnapshot {
    val event = Event(
        id = "event-1", organizerId = "user-1", title = title,
        description = "Study group", startsAt = Instant.parse("2026-11-01T12:00:00Z"),
        endsAt = null, zoneId = "Europe/Berlin", location = "Library"
    )
    val member = EventParticipant(
        event.id, Participant("user-1", "Alex"), ParticipantRole.ORGANIZER
    )
    return EventsSnapshot(listOf(EventDetails(event, listOf(member))), Instant.EPOCH)
}

private fun withModel(
    repository: EventRepository,
    block: (EventsViewModel) -> Unit
) {
    val store = ViewModelStore()
    val model = EventsViewModel(repository)
    store.put("events", model)
    try { block(model) } finally { store.clear() }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun loadingThenSuccessThenLiveUpdate() = runTest {
        val repository = FakeEventRepository()
        val gate = CompletableDeferred<Unit>()
        repository.refreshAction = {
            gate.await()
            repository.snapshots.value = sample("Study group")
        }
        withModel(repository) { model ->
            assertEquals(EventsUiState.Loading, model.uiState.value)
            runCurrent()
            assertEquals(EventsUiState.Loading, model.uiState.value)
            gate.complete(Unit)
            runCurrent()
            assertEquals("Study group", (model.uiState.value as
                EventsUiState.Success).events.single().event.title)
            repository.snapshots.value = sample("New meeting location")
            runCurrent()
            assertEquals("New meeting location", (model.uiState.value as
                EventsUiState.Success).events.single().event.title)
            assertEquals(1, repository.refreshCalls)
        }
        runCurrent()
        assertEquals(0, repository.activeObservers)
    }

    @Test fun failedRefreshKeepsCachedContent() = runTest {
        val repository = FakeEventRepository()
        repository.snapshots.value = sample("Cached event")
        repository.refreshAction = { throw IOException("Offline") }
        withModel(repository) { model ->
            runCurrent()
            val state = model.uiState.value as EventsUiState.Success
            assertEquals("Cached event", state.events.single().event.title)
            assertNotNull(state.refreshError)
            assertFalse(state.refreshing)
        }
    }

    @Test fun noCacheFailureCanBeRetried() = runTest {
        val repository = FakeEventRepository()
        repository.refreshAction = { throw IOException("Offline") }
        withModel(repository) { model ->
            runCurrent()
            assertTrue(model.uiState.value is EventsUiState.Error)
            repository.refreshAction = {
                repository.snapshots.value = sample("Recovered")
            }
            model.loadEvents()
            runCurrent()
            assertNull((model.uiState.value as EventsUiState.Success).refreshError)
            assertEquals(2, repository.refreshCalls)
        }
    }

    @Test fun initializedEmptySnapshotIsSuccess() = runTest {
        val repository = FakeEventRepository()
        repository.refreshAction = {
            repository.snapshots.value = EventsSnapshot(emptyList(), Instant.EPOCH)
        }
        withModel(repository) { model ->
            runCurrent()
            val state = model.uiState.value as EventsUiState.Success
            assertTrue(state.events.isEmpty())
            assertFalse(state.refreshing)
        }
    }

    @Test fun repeatedLoadsDoNotOverlapAndClearingCancels() = runTest {
        val repository = FakeEventRepository()
        val gate = CompletableDeferred<Unit>()
        var cancelled = false
        repository.refreshAction = {
            try { gate.await() } finally { cancelled = true }
        }
        withModel(repository) { model ->
            runCurrent()
            model.loadEvents()
            model.loadEvents()
            runCurrent()
            assertEquals(1, repository.refreshCalls)
            assertEquals(1, repository.activeObservers)
        }
        runCurrent()
        assertTrue(cancelled)
        assertEquals(0, repository.activeObservers)
    }
}
