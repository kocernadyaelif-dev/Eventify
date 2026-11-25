
package eventify

import androidx.room.Embedded
import androidx.room.Relation

// A membership and its associated participant
data class MembershipWithParticipant(

    @Embedded
    val membership: EventMembershipEntity,

    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val participant: ParticipantEntity
)

// An event and all its memberships
data class EventWithParticipants(

    @Embedded
    val event: EventEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "eventId",
        entity = EventMembershipEntity::class
    )
    val memberships: List<MembershipWithParticipant>
)

// Convert Room data into the existing domain models
fun EventWithParticipants.toDomain(): EventDetails {

    return EventDetails(
        event = event.toDomain(),
        participants = memberships.map { item ->
            item.membership.toDomain(item.participant)
        }
    )
}
