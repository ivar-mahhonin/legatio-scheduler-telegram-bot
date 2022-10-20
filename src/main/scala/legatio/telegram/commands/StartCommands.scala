package legatio.telegram.commands

import cats.implicits.toFunctorOps
import com.bot4s.telegram.future.{Polling, TelegramBot}

trait StartCommands extends BotCommands { this: TelegramBot with Polling =>
  onCommand(StartCommands.START.toString) { implicit msg =>
    logger.info("[TGBot] 'start' command received")
    reply("Bot is online :)").void
  }
}
object StartCommands extends Enumeration {
  val START: Value = Value(1, "start")
}
