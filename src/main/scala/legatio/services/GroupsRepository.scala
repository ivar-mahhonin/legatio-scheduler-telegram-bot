package legatio.services

import legatio.db.SlickTables
import legatio.db.tables.GroupsTable
import legatio.models.Group
import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._

object GroupsRepository extends Repository[Group, GroupsTable](SlickTables.groupsTable) {
  def deleteByExternalId(id: Long): Future[Int] = db.run {
    table.filter(_.externalId === id).delete
  }
}