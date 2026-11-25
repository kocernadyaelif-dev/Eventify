package eventify

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun EventsRoute(viewModel: EventsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EventsScreen(state, onRetry = viewModel::loadEvents)
}

@Composable
fun EventsScreen(state: EventsUiState, onRetry: () -> Unit) {
    when (state) {
        EventsUiState.Loading -> CircularProgressIndicator()
        is EventsUiState.Error -> Column {
            Text(state.message)
            Button(onClick = onRetry) { Text("Retry") }
        }
        is EventsUiState.Success -> Column {
            if (state.refreshing) LinearProgressIndicator()
            state.refreshError?.let { Text(it) }
            Text("Last synced: ${state.lastSyncedAt ?: "Not yet synced"}")
            if (state.hasPendingWrites) Text("Changes waiting to sync")
            Button(onClick = onRetry, enabled = !state.refreshing) {
                Text("Refresh")
            }
            if (state.events.isEmpty()) Text("No events yet")
            LazyColumn {
                items(state.events, key = { it.event.id }) { details ->
                    val going = details.participants.count {
                        it.rsvp == RsvpStatus.GOING
                    }
                    Text(details.event.title)
                    Text(details.event.location)
                    Text("${details.event.startsAt} (${details.event.zoneId})")
                    Text("${details.event.status}: $going going")
                }
            }
        }
    }
}
