
package eventify

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "event_memberships",
    primaryKeys = ["eventId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"])
    ]
)
data class EventMembershipEntity(
    val eventId: String,
    val userId: String,
    val role: String,
    val rsvp: String,
    val serverRevision: Long
)

fun EventParticipant.toEntity(): EventMembershipEntity {
    return EventMembershipEntity(
        eventId = eventId,
        userId = participant.userId,
        role = role.name,
        rsvp = rsvp.name,
        serverRevision = serverRevision
    )
}

fun EventMembershipEntity.toDomain(
    participantEntity: ParticipantEntity
): EventParticipant {

    require(userId == participantEntity.userId) {
        "Participant does not match membership"
    }

    return EventParticipant(
        eventId = eventId,
        participant = participantEntity.toDomain(),
        role = ParticipantRole.valueOf(role),
        rsvp = RsvpStatus.valueOf(rsvp),
        serverRevision = serverRevision
    )
}
