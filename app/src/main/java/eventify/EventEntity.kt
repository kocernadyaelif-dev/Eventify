package eventify

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "events")
data class EventEntity(

    @PrimaryKey
    val id: String,

    val organizerId: String,

    val title: String,

    val description: String,

    val startsAtEpochMillis: Long,

    val endsAtEpochMillis: Long?,

    val zoneId: String,

    val location: String,

    val status: String,

    val serverRevision: Long
)

// Convert domain Event into a Room entity
fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        organizerId = organizerId,
        title = title,
        description = description,
        startsAtEpochMillis = startsAt.toEpochMilli(),
        endsAtEpochMillis = endsAt?.toEpochMilli(),
        zoneId = zoneId,
        location = location,
        status = status.name,
        serverRevision = serverRevision
    )
}

// Convert Room entity back into domain Event
fun EventEntity.toDomain(): Event {
    return Event(
        id = id,
        organizerId = organizerId,
        title = title,
        description = description,
        startsAt = Instant.ofEpochMilli(startsAtEpochMillis),
        endsAt = endsAtEpochMillis?.let {
            Instant.ofEpochMilli(it)
        },
        zoneId = zoneId,
        location = location,
        status = EventStatus.valueOf(status),
        serverRevision = serverRevision
    )
}
