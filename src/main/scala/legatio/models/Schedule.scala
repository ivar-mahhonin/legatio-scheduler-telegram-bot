package legatio.models

final case class Schedule(
                           id: Option[Long] = None,
                           text: String,
                           date: Long,
                           createdDate: Option[Long] = None,
                           updatedDate: Option[Long] = None) {
}
