package legatio

import legatio.telegram.TGBot
import scala.concurrent.Await
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

object Legatio extends App {
  val config = ConfigFactory.load().getConfig("telegram")
  val botToken = config.getString("bot-token")
  if (botToken.nonEmpty) {
    val bot = new TGBot(botToken)
    val eol = bot.run()
    println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
    scala.io.StdIn.readLine()
    bot.shutdown()
    Await.result(eol, Duration.Inf)
  }
  else {
    throw new RuntimeException("Telegram bot token missing at 'telegram-bot.conf'")
  }
}

