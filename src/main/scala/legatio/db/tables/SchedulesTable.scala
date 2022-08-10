package legatio.db.tables

import legatio.models.Schedule

object SlickTables {

  import slick.jdbc.PostgresProfile.api._

  class SchedulesTable(tag: Tag) extends Table[Schedule](tag, Some("schedules"), "Schedule") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def text = column[String]("text")

    def date = column[Long]("date")

    def createdDate = column[Option[Long]]("created_date")

    def updatedDate = column[Option[Long]]("updated_date")

    def * = (
      id,
      text,
      date,
      createdDate,
      updatedDate,
    ) <> (Schedule.tupled, Schedule.unapply)
  }

  lazy val schedulesTable = TableQuery[SchedulesTable]
}

