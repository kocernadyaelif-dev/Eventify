
package eventify

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomEventRepository(
    private val database: EventifyDatabase,
    private val demoEvents: List<EventDetails>
) : EventRepository {

    private val dao = database.eventDao()

    // Read stored events reactively from Room
    override fun observeEvents(): Flow<EventsSnapshot?> {
        return dao.observeEventsWithParticipants()
            .map { storedEvents ->

                EventsSnapshot(
                    events = storedEvents.map {
                        it.toDomain()
                    },
                    lastSyncedAt = null,
                    hasPendingWrites = false
                )
            }
    }

    // Initialize the local database with demo events
    override suspend fun refreshEvents() {

        database.withTransaction {

            if (dao.countEvents() == 0) {

                demoEvents.forEach { details ->
                    dao.saveEventDetails(details)
                }
            }
        }
    }
}
