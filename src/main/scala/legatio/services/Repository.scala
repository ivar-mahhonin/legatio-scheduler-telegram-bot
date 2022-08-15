package legatio.services

import legatio.db.DbConnection
import legatio.db.tables.BaseTable
import legatio.models.BaseEntity
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

class Repository[E <: BaseEntity, T <: Table[E] with BaseTable[E]]( t: => TableQuery[T]) {
  protected lazy val table = t
  protected lazy val db = DbConnection.db

  def getById(id: Long): Future[Option[E]] = db.run {
    getByIdQuery(id).result.headOption
  }

  def insert(entity: E): Future[Int] = db.run {
    table += entity
  }

  def delete(id: Long): Future[Int] = db.run {
    getByIdQuery(id).delete
  }

  def update(id: Long, event: E): Future[Int] = db.run {
    getByIdQuery(id).update(event)
  }

  private def getByIdQuery(id: Long) = table.filter(_.id === id)
}