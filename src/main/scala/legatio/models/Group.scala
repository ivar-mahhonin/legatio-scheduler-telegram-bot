package legatio.models

import java.sql.Timestamp

final case class Group(id: Option[Long] = None,
                       externalId: Long,
                       isChannel: Boolean = false,
                       isGroup: Boolean = false,
                       createdDate: Option[Timestamp] = None,
                       updatedDate: Option[Timestamp] = None) extends BaseEntity