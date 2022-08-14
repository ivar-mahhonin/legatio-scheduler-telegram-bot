package legatio.db

import legatio.models.{BaseEntity, Group, Schedule}

object SlickTables {

  import slick.jdbc.PostgresProfile.api._

  trait BaseTable[T <: BaseEntity] {
    this: Table[T] =>
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def createdDate = column[Option[Long]]("created_date")

    def updatedDate = column[Option[Long]]("updated_date")
  }

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

  class GroupsTable(tag: Tag) extends Table[Group](tag, Some("schedules"), "Group") with BaseTable[Group] {

    def externalId = column[Long]("external_id")
    def isChannel = column[Boolean]("is_channel")
    def isGroup = column[Boolean]("is_group")


    def * = (
      id,
      externalId,
      isChannel,
      isGroup,
      createdDate,
      updatedDate,
    ) <> (Group.tupled, Group.unapply)
  }

  lazy val schedulesTable: TableQuery[SchedulesTable] = TableQuery[SchedulesTable]
  lazy val groupsTable: TableQuery[GroupsTable] = TableQuery[GroupsTable]

}

