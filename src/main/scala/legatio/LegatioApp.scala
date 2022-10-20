package legatio

import legatio.telegram.TGBot
import com.typesafe.config.ConfigFactory
import legatio.db.DbConnection

object LegatioApp {
  def main(args: Array[String]): Unit = {
    DbConnection.testDbConnection(() => startBot())
  }

  private def startBot() = {
    val config = ConfigFactory.load().getConfig("telegram")
    val botToken = config.getString("bot-token")

    if (botToken.isEmpty) {
      throw new RuntimeException("Telegram bot token missing at 'telegram-bot.conf'")
    }
    else {
      val bot = TGBot(botToken)
      bot.run()
    }
  }
}

