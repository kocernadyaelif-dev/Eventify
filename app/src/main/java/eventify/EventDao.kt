
package eventify

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Transaction
    @Query(
        "SELECT * FROM events ORDER BY startsAtEpochMillis ASC"
    )
    fun observeEventsWithParticipants():
            Flow<List<EventWithParticipants>>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun countEvents(): Int

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(
        eventId: String
    ): EventEntity?

    @Upsert
    suspend fun upsertEvent(
        event: EventEntity
    )

    @Upsert
    suspend fun upsertEvents(
        events: List<EventEntity>
    )

    @Upsert
    suspend fun upsertParticipants(
        participants: List<ParticipantEntity>
    )

    @Upsert
    suspend fun upsertMemberships(
        memberships: List<EventMembershipEntity>
    )

    @Query(
        "DELETE FROM event_memberships WHERE eventId = :eventId"
    )
    suspend fun deleteMembershipsForEvent(
        eventId: String
    )

    @Transaction
    suspend fun saveEventDetails(
        details: EventDetails
    ) {
        upsertEvent(details.event.toEntity())

        upsertParticipants(
            details.participants.map {
                it.participant.toEntity()
            }.distinctBy { it.userId }
        )

        deleteMembershipsForEvent(details.event.id)

        upsertMemberships(
            details.participants.map {
                it.toEntity()
            }
        )
    }
}
