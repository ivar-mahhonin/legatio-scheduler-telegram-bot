package legatio.db

import slick.jdbc.PostgresProfile.api._
import scala.util._

object DbConnection {
  lazy val db = Database.forConfig("database")

  def testDbConnection(callback: () => Unit, attempts: Int = 3): Unit = Try(db.createSession.conn) match {
    case Success(con) =>
      println("Connected to the database.")
      con.close()
      callback()
    case _ =>
      if (attempts == 0) {
        println("Could not connect to database 3 times. Exiting...")
        scala.sys.exit()
      }
      else {
        println("Could not connect to database. Reconnecting...")
        Thread.sleep(3000)
        testDbConnection(callback, attempts - 1)
      }
  }
}