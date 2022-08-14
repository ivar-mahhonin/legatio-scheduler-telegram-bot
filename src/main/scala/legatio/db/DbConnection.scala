package legatio.db

import slick.jdbc.PostgresProfile.api._

object DbConnection {
  lazy val db = Database.forConfig("postgres")
}