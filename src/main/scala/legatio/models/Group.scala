package legatio.models

final case class Group(id: Option[Long] = None,
                       externalId: Long,
                       isChannel: Boolean = false,
                       isGroup: Boolean = false,
                       createdDate: Option[Long] = None,
                       updatedDate: Option[Long] = None) extends BaseEntity