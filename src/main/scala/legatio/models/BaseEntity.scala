package legatio.models

import java.sql.Timestamp

trait BaseEntity {
  val id: Option[Long]
  val createdDate: Option[Timestamp]
  val updatedDate: Option[Timestamp]
}