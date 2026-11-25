
package eventify

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDaoTest {

    private lateinit var database: EventifyDatabase
    private lateinit var dao: EventDao

    @Before
    fun setup() {
        val context: Context =
            InstrumentationRegistry.getInstrumentation()
                .targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            EventifyDatabase::class.java
        ).build()

        dao = database.eventDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // Sample event used by all tests
    private fun sampleEvent(
        title: String = "Kotlin Workshop",
        rsvp: RsvpStatus = RsvpStatus.GOING
    ): EventDetails {

        val event = Event(
            id = "event-test-1",
            organizerId = "user-1",
            title = title,
            description = "Learn Android development",
            startsAt = Instant.parse(
                "2026-10-24T13:00:00Z"
            ),
            endsAt = null,
            zoneId = "Europe/Berlin",
            location = "Berlin"
        )

        val membership = EventParticipant(
            eventId = event.id,
            participant = Participant(
                userId = "user-2",
                displayName = "Anna"
            ),
            role = ParticipantRole.GUEST,
            rsvp = rsvp
        )

        return EventDetails(
            event = event,
            participants = listOf(membership)
        )
    }

    // ------------------------------------------
    // TEST 1: Save and read an event
    // ------------------------------------------

    @Test
    fun saveAndReadEvent() = runBlocking {

        dao.saveEventDetails(sampleEvent())

        val storedEvent = dao.getEventById(
            "event-test-1"
        )

        assertNotNull(storedEvent)

        assertEquals(
            "Kotlin Workshop",
            storedEvent?.title
        )

        assertEquals(
            "Berlin",
            storedEvent?.location
        )
    }

    // ------------------------------------------
    // TEST 2: Read participant relationships
    // ------------------------------------------

    @Test
    fun loadEventWithParticipants() = runBlocking {

        dao.saveEventDetails(sampleEvent())

        val events = dao
            .observeEventsWithParticipants()
            .first()

        val details = events.single().toDomain()

        assertEquals(
            "Kotlin Workshop",
            details.event.title
        )

        assertEquals(
            "Anna",
            details.participants.single()
                .participant.displayName
        )

        assertEquals(
            RsvpStatus.GOING,
            details.participants.single().rsvp
        )
    }

    // ------------------------------------------
    // TEST 3: Update event and RSVP
    // ------------------------------------------

    @Test
    fun updateEventAndRsvp() = runBlocking {

        dao.saveEventDetails(sampleEvent())

        dao.saveEventDetails(
            sampleEvent(
                title = "Kotlin Workshop Updated",
                rsvp = RsvpStatus.MAYBE
            )
        )

        val events = dao
            .observeEventsWithParticipants()
            .first()

        assertEquals(1, events.size)

        val details = events.single().toDomain()

        assertEquals(
            "Kotlin Workshop Updated",
            details.event.title
        )

        assertEquals(
            RsvpStatus.MAYBE,
            details.participants.single().rsvp
        )
    }

    // ------------------------------------------
    // TEST 4: Reactive Room Flow updates
    // ------------------------------------------

    @Test
    fun roomFlowEmitsAfterEventUpdate() = runBlocking {

        // Save the initial event
        dao.saveEventDetails(sampleEvent())

        val firstObserved = CompletableDeferred<Unit>()

        // Collect Room Flow before updating data
        val updatedTitle = async {

            dao.observeEventsWithParticipants()
                .map { events ->
                    events.single().event.title
                }
                .first { title ->

                    if (title == "Kotlin Workshop") {
                        firstObserved.complete(Unit)
                    }

                    title == "Kotlin Workshop Updated"
                }
        }

        // Confirm that the first value was observed
        withTimeout(10000) {
            firstObserved.await()
        }

        // Update the saved event
        dao.saveEventDetails(
            sampleEvent(
                title = "Kotlin Workshop Updated"
            )
        )

        // Wait for the new value from Room Flow
        val result = withTimeout(10000) {
            updatedTitle.await()
        }

        assertEquals(
            "Kotlin Workshop Updated",
            result
        )
    }
}
