
package eventify

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        EventEntity::class,
        ParticipantEntity::class,
        EventMembershipEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EventifyDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
}
