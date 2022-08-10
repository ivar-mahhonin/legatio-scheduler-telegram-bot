package legatio.db.tables

import slick.jdbc.PostgresProfile.api._

object DbConnection {
  val db = Database.forConfig("postgres")
}