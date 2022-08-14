package legatio.models
final case class Schedule(
                           id: Option[Long] = None,
                           userId: Long,
                           groupId: Long,
                           text: String,
                           date: Long,
                           createdDate: Option[Long] = None,
                           updatedDate: Option[Long] = None) extends BaseEntity