package legatio.models

trait BaseEntity {
  val id: Option[Long]
  val createdDate: Option[Long]
  val updatedDate: Option[Long]
}