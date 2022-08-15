package legatio.db

import legatio.db.tables.{GroupsTable, SchedulesTable}
import slick.lifted.TableQuery

object SlickTables {
  lazy val schedulesTable: TableQuery[SchedulesTable] = TableQuery[SchedulesTable]
  lazy val groupsTable: TableQuery[GroupsTable] = TableQuery[GroupsTable]
}

