package legatio.models

import java.sql.Timestamp

final case class Schedule(
                           id: Option[Long] = None,
                           userId: Long,
                           groupId: Long,
                           text: String,
                           date: Long,
                           createdDate: Option[Timestamp],
                           updatedDate: Option[Timestamp]) extends BaseEntity