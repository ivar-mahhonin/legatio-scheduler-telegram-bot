package legatio.db.tables

import legatio.models.BaseEntity
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp

trait BaseTable[T <: BaseEntity] {
  this: Table[T] =>
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

  def createdDate = column[Option[Timestamp]]("created_date", O.AutoInc)

  def updatedDate = column[Option[Timestamp]]("updated_date", O.AutoInc)
}