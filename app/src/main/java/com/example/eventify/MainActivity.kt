

package com.example.eventify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.eventify.ui.theme.EventifyTheme

import eventify.DemoEventRepository
import eventify.Event
import eventify.EventDetails
import eventify.EventParticipant
import eventify.EventsRoute
import eventify.EventsViewModel
import eventify.Participant
import eventify.RsvpStatus

import java.time.Instant

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventsViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    if (modelClass == EventsViewModel::class.java) {
                        return EventsViewModel(
                            DemoEventRepository(createDemoEvents())
                        ) as T
                    }
                    throw IllegalArgumentException(
                        "Unknown ViewModel class"
                    )
                }
            }
        )[EventsViewModel::class.java]

        setContent {
            EventifyTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        EventsRoute(
                            viewModel = eventsViewModel
                        )
                    }
                }
            }
        }
    }
}

private fun createDemoEvents(): List<EventDetails> {

    val event1 = Event(
        id = "event-1",
        organizerId = "user-1",
        title = "Kotlin Workshop",
        description = "Learn Android development",
        startsAt = Instant.parse("2026-10-24T13:00:00Z"),
        endsAt = Instant.parse("2026-10-24T15:00:00Z"),
        zoneId = "Europe/Berlin",
        location = "Berlin"
    )

    val event2 = Event(
        id = "event-2",
        organizerId = "user-2",
        title = "Tech Networking Meetup",
        description = "Meet other developers",
        startsAt = Instant.parse("2026-10-30T17:00:00Z"),
        endsAt = Instant.parse("2026-10-30T19:00:00Z"),
        zoneId = "Europe/Berlin",
        location = "Munich"
    )

    return listOf(
        EventDetails(
            event = event1,
            participants = listOf(
                EventParticipant(
                    eventId = event1.id,
                    participant = Participant(
                        userId = "user-3",
                        displayName = "Anna"
                    ),
                    rsvp = RsvpStatus.GOING
                )
            )
        ),
        EventDetails(
            event = event2,
            participants = listOf(
                EventParticipant(
                    eventId = event2.id,
                    participant = Participant(
                        userId = "user-4",
                        displayName = "Max"
                    ),
                    rsvp = RsvpStatus.GOING
                )
            )
        )
    )
}
