package eventify

import java.time.Instant

enum class RsvpStatus { PENDING, GOING, MAYBE, DECLINED }
enum class ParticipantRole { ORGANIZER, GUEST }
enum class EventStatus { SCHEDULED, CANCELLED }

data class Participant(
    val userId: String,
    val displayName: String
)

data class EventParticipant(
    val eventId: String,
    val participant: Participant,
    val role: ParticipantRole = ParticipantRole.GUEST,
    val rsvp: RsvpStatus = RsvpStatus.PENDING,
    val serverRevision: Long = 0
)

data class Event(
    val id: String,
    val organizerId: String,
    val title: String,
    val description: String,
    val startsAt: Instant,
    val endsAt: Instant?,
    val zoneId: String,
    val location: String,
    val status: EventStatus = EventStatus.SCHEDULED,
    val serverRevision: Long = 0
)

data class EventDetails(
    val event: Event,
    val participants: List<EventParticipant>
)

// A non-null snapshot, even with an empty list, means an initialized cache.
// hasPendingWrites and lastSyncedAt come from persisted sync metadata.
data class EventsSnapshot(
    val events: List<EventDetails>,
    val lastSyncedAt: Instant?,
    val hasPendingWrites: Boolean = false
)
