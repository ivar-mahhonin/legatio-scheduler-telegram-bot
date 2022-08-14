package legatio.telegram

import akka.util.Timeout
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.{Commands, Updates}
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import legatio.actors.{GroupsRepositoryManager, RepositoryManagers}
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}
import com.bot4s.telegram.methods.{GetMe, SetMyCommands}
import com.bot4s.telegram.models.{BotCommand, ChatId}
import legatio.actors.RepositoryManagers.system

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class TGBot(token: String) extends TelegramBot with Polling with Commands[Future] with Updates[Future] {
  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  import akka.actor.typed.scaladsl.AskPattern._

  override val client: RequestHandler[Future] = new ScalajHttpClient(token)
  implicit val timeout: Timeout = 3.seconds
  private var botIDOpt: Option[Long] = None

  override def run(): Future[Unit] = {
    logger.info(s"[TGBot] starting")
    lazy val runFuture = super.run()

    request(GetMe).onComplete {
      case Success(user) =>
        logger.info(s"[TGBot] 'getMe' received. Bot id ${user.id}, bot name: ${user.username}")
        botIDOpt = Some(user.id)
      case Failure(ex) => logger.info(s"[TGBot] 'getMe' failed. Reason: $ex")
    }
    runFuture
  }

  private def registerBotInNewGroup(chatId: Long, isChannel: Boolean, isChat: Boolean) =
    RepositoryManagers.groups.ask(ref => GroupsRepositoryManager.RegisterInGroup(chatId, isChannel, isChat, ref))

  private def removeBotFromExistingGroup(chatId: Long) =
    RepositoryManagers.groups.ask(ref => GroupsRepositoryManager.RemoveFromGroup(chatId, ref))

  private def chatIdToLong(chatId: ChatId) = {
    chatId.toEither match {
      case Left(l) => l
      case Right(s) => s.toLong
    }
  }

  request(
    SetMyCommands(
      List(
        BotCommand("schedule", "Schedule a message"),
        BotCommand("list", "List existing schedules"),
        BotCommand("groups", "List groups to which bot belongs"),
      )
    )
  ).void

  onUpdate { action =>
    if (botIDOpt.isDefined) {
      val chatMembersInfoOpt = for {
        m <- action.message
        leftChatMember = m.leftChatMember
        newChatMembers = m.newChatMembers
      } yield (m.chat.chatId, leftChatMember, newChatMembers)

      val botID = botIDOpt.get
      chatMembersInfoOpt match {
        case Some((chat: ChatId, Some(leftMember), _)) =>
          if (leftMember.isBot && leftMember.id == botID) {
            val removeFuture = removeBotFromExistingGroup(chatIdToLong(chat))
            removeFuture.onComplete {
              case Success(GroupsRepositoryManager.RegisterInGroupSuccess) => println(s"[TGBot] Successfully removed from group[$chat]")
              case Failure(ex) => println(s"[TGBot] Could not remove from group[$chat]: $ex")
            }
          }
        case Some((chat: ChatId, _, Some(joinedMembers))) =>
          val botJoinedOpt = joinedMembers.find(u => u.isBot && u.id == botID)
          botJoinedOpt match {
            case Some(_) =>
              val registerFuture = registerBotInNewGroup(chatIdToLong(chat), chat.isChannel, chat.isChat)
              registerFuture.onComplete {
                case Success(GroupsRepositoryManager.RegisterInGroupSuccess) => println(s"[TGBot] Successfully registered bot in new group[$chat]")
                case Failure(ex) => println(s"[TGBot] Could not register bot in new group[$chat]: $ex")
              }
            case _ =>
          }
        case _ =>
      }
    }
    Future(())
  }

  onCommand("start") { implicit msg =>
    logger.info("[TGBot] 'start' command received")
    reply("Hello!").void
  }
}