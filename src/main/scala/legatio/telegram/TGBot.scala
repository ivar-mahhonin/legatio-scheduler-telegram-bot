package legatio.telegram

import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.{Callbacks, JoinRequests, Updates}
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.{GetMe, SetMyCommands}
import com.bot4s.telegram.models.{BotCommand, ChatId, Update, User}
import scala.concurrent.Future
import scala.util.{Failure, Success}
import legatio.telegram.commands.BotCommands.makeIgnoreProtocolMessage
import legatio.telegram.commands.groups.{GroupCommands, GroupCommandsService}
import legatio.telegram.commands.StartCommands
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

class TGBot(token: String)
    extends TelegramBot
    with Polling
    with Updates[Future]
    with JoinRequests[Future]
    with Callbacks[Future]
    with StartCommands
    with GroupCommands {
  override val client: RequestHandler[Future] = new ScalajHttpClient(token)
  private var botIDOpt: Option[Long] = None

  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  override def run(): Future[Unit] = {
    logger.info(s"[TGBot] starting")
    request(GetMe).onComplete {
      case Success(user) =>
        logger.info(s"[TGBot] 'getMe' received. Bot id ${user.id}, bot name: ${user.username}")
        botIDOpt = Some(user.id)
      case Failure(ex) => logger.info(s"[TGBot] 'getMe' failed. Reason: $ex")
    }
    super.run()
  }

  request(
    SetMyCommands(
      List(
        BotCommand(StartCommands.START.toString, "Test if bot is online"),
        BotCommand("list", "List existing schedules"),
        BotCommand(GroupCommands.LIST_GROUPS.toString, "List User groups to manage"),
      )
    )
  ).void

  onJoinRequest { _ =>
    logger.info("[TGBot] On Join Request")
    Future()
  }

  // TODO handle addition to groups. Current approach works only for chats
  // TODO handle case, when bot is added to the group and bot was offline
  onUpdate { update =>
    val botID = botIDOpt.get

    if (botIDOpt.isDefined) {
      val chatMembersInfoOpt = TGBot.getChatMemberInfo(update)
      val res: Future[TelegramBotProtocol] = chatMembersInfoOpt match {
        case Some((chatTitle: String, chatId: ChatId, _, Some(joinedMembers))) =>
          GroupCommandsService.ifBotJoinedGroup(botID, chatTitle, chatId, joinedMembers)
        case Some((_, chatId: ChatId, Some(leftMember), _)) =>
          GroupCommandsService.ifBotLeftGroup(botID, chatId, leftMember)
        case _ => makeIgnoreProtocolMessage("Event not supported")
      }
      res.map(p => logger.info(p.toString))
    } else Future(())
  }

  onCallbackQuery { callback =>
    for {
      msg <- callback.message
      cbd <- callback.data
      answer = cbd.trim
      _ = println(cbd)
    } yield ()
    Future()
  }
}

object TGBot {
  def apply(token: String): TGBot = new TGBot(token)

  def getChatMemberInfo(update: Update): Option[(String, ChatId, Option[User], Option[Array[User]])] =
    for {
      m <- update.message
      chat = m.chat
      title <- chat.title
      leftChatMember = m.leftChatMember
      newChatMembers = m.newChatMembers
    } yield (title, chat.chatId, leftChatMember, newChatMembers)
}
