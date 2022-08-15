package legatio.db.tables

import slick.jdbc.PostgresProfile.api._
import legatio.models.Schedule

class SchedulesTable(tag: Tag) extends Table[Schedule](tag, Some("schedules"), "Schedule") with BaseTable[Schedule] {
  def userId = column[Long]("user_id")

  def groupId = column[Long]("user_id")

  def text = column[String]("text")

  def date = column[Long]("date")


  def * = (
    id,
    userId,
    groupId,
    text,
    date,
    createdDate,
    updatedDate,
  ) <> (Schedule.tupled, Schedule.unapply)
}
