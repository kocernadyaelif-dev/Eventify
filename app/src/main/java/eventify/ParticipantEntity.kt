
package eventify

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participants")
data class ParticipantEntity(

    @PrimaryKey
    val userId: String,

    val displayName: String
)

// Convert domain Participant into Room entity
fun Participant.toEntity(): ParticipantEntity {
    return ParticipantEntity(
        userId = userId,
        displayName = displayName
    )
}

// Convert Room entity back into domain Participant
fun ParticipantEntity.toDomain(): Participant {
    return Participant(
        userId = userId,
        displayName = displayName
    )
}
