package legatio.telegram.commands

import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.models.ChatId
import legatio.telegram.TelegramBotProtocol.TelegramIgnoreProtocol

import scala.concurrent.{ExecutionContext, Future}

trait BotCommands extends Commands[Future]{ this: TelegramBot with Polling =>
}

object BotCommands {
  def makeIgnoreProtocolMessage(msg: String)(
    implicit executor: ExecutionContext
  ): Future[TelegramIgnoreProtocol] = Future(TelegramIgnoreProtocol(msg))

  def chatIdToLong(chatId: ChatId): Long = chatId.toEither match {
    case Left(l) => l
    case Right(s) => s.toLong
  }
}
