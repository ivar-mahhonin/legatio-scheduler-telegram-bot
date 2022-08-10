package legatio.db.services

import legatio.db.tables.{DbConnection, SlickTables}
import legatio.models.Schedule

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._

object SchedulesRepository {
  lazy val db = DbConnection.db
  lazy val Schedules = SlickTables.schedulesTable

  def list(): Future[Seq[Schedule]] = db.run(Schedules.result)

  def getById(id: Long): Future[Option[Schedule]] = db.run {
    getByIdQuery(id).result.headOption
  }

  def insert(schedule: Schedule): Future[Int] = db.run {
    Schedules += schedule
  }

  def delete(id: Long): Future[Int] = db.run {
    getByIdQuery(id).delete
  }

  def update(id: Long, event: Schedule): Future[Int] = db.run {
    getByIdQuery(id).update(event)
  }

  private def getByIdQuery(id: Long) = Schedules.filter(_.id === id)
}