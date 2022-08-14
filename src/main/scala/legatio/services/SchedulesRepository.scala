package legatio.services

import legatio.db.SlickTables
import legatio.db.SlickTables.SchedulesTable
import legatio.models.Schedule

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._

object SchedulesRepository extends Repository[Schedule, SchedulesTable](SlickTables.schedulesTable) {
  def listUserSchedulesGroupId(groupId: Long, userId: Long): Future[Seq[Schedule]] = db.run(table
    .filter(sch =>sch.groupId === groupId && sch.userId === userId).result)
  def listAllByUserId(userId: Long): Future[Seq[Schedule]] = db.run(table.filter(_.userId === userId).result)
  def listAllByGroupId(groupId: Long): Future[Seq[Schedule]] = db.run(table.filter(_.groupId === groupId).result)
  def listAll(userId: Long): Future[Seq[Schedule]] = db.run(table.filter(_.userId === userId).result)
}