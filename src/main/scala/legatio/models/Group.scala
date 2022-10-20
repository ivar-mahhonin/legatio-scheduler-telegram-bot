package legatio.models

import java.sql.Timestamp

final case class Group(name: String,
                       externalId: Long,
                       isChannel: Boolean = false,
                       id: Option[Long] = None,
                       createdDate: Option[Timestamp] = None,
                       updatedDate: Option[Timestamp] = None) extends BaseEntity