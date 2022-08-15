package legatio.db.tables
import legatio.models.Group
import slick.jdbc.PostgresProfile.api._

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
