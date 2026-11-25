
package eventify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventsRoute(viewModel: EventsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    EventsScreen(
        state = state,
        onRetry = viewModel::loadEvents
    )
}

@Composable
fun EventsScreen(
    state: EventsUiState,
    onRetry: () -> Unit
) {
    when (state) {
        EventsUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is EventsUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Something went wrong",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = onRetry) {
                    Text("Try Again")
                }
            }
        }

        is EventsUiState.Success -> {
            EventsContent(state, onRetry)
        }
    }
}

@Composable
private fun EventsContent(
    state: EventsUiState.Success,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Eventify",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Discover and manage your events",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "Your Events",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${state.events.size} events available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onRefresh,
                    enabled = !state.refreshing
                ) {
                    if (state.refreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refreshing")
                    } else {
                        Text("Refresh")
                    }
                }

                if (state.refreshing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                state.refreshError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Last synced: " +
                            (state.lastSyncedAt?.let {
                                formatEventDate(
                                    it,
                                    ZoneId.systemDefault().id
                                )
                            } ?: "Not yet synced"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (state.hasPendingWrites) {
                    Text(
                        text = "Changes waiting to sync",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        if (state.events.isEmpty()) {
            item {
                Text(
                    text = "No events yet",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }

        items(
            items = state.events,
            key = { it.event.id }
        ) { details ->
            EventCard(details)
        }
    }
}

@Composable
private fun EventCard(details: EventDetails) {
    val event = details.event

    val goingCount = details.participants.count {
        it.rsvp == RsvpStatus.GOING
    }

    val cancelled = event.status == EventStatus.CANCELLED

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = if (cancelled) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = if (cancelled) {
                        "Cancelled"
                    } else {
                        "Scheduled"
                    },
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "📅 " + formatEventDate(
                    event.startsAt,
                    event.zoneId
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "📍 ${event.location}",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = "👥 $goingCount participant(s) going",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatEventDate(
    instant: Instant,
    zoneId: String
): String {
    val zone = runCatching {
        ZoneId.of(zoneId)
    }.getOrDefault(ZoneId.systemDefault())

    val formatter = DateTimeFormatter.ofPattern(
        "dd MMM yyyy • HH:mm",
        Locale.ENGLISH
    )

    return instant.atZone(zone).format(formatter)
}
