package legatio.telegram

import akka.util.Timeout
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.{Commands, JoinRequests, Updates}
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import legatio.actors.{GroupsRepositoryService, RepositoryServices}
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}
import com.bot4s.telegram.methods.{GetMe, SetMyCommands}
import com.bot4s.telegram.models.{BotCommand, ChatId}
import legatio.actors.RepositoryServices.system

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.actor.typed.scaladsl.AskPattern._
import legatio.telegram.TelegramBotProtocol.{FailureProtocol, IgnoreProtocol, SuccessProtocol}

sealed trait TelegramBotProtocol

object TelegramBotProtocol {
  case class SuccessProtocol(msg: String) extends TelegramBotProtocol

  case object IgnoreProtocol extends TelegramBotProtocol

  case class FailureProtocol(msg: String) extends TelegramBotProtocol
}


class TGBot(token: String) extends TelegramBot with Polling with Commands[Future] with Updates[Future] with JoinRequests[Future] {
  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  override val client: RequestHandler[Future] = new ScalajHttpClient(token)
  implicit val timeout: Timeout = 3.seconds

  private var botIDOpt: Option[Long] = None
  private val groupsServiceManager = RepositoryServices.groups
  private val schedulesServiceManager = RepositoryServices.schedules


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

  private def chatIdToLong(chatId: ChatId): Long = chatId.toEither match {
    case Left(l) => l
    case Right(s) => s.toLong
  }

  private def registerBotInGroup(chatId: ChatId): Future[TelegramBotProtocol] = {
    groupsServiceManager.ask(ref => GroupsRepositoryService.RegisterInGroup(
      chatIdToLong(chatId), chatId.isChannel, chatId.isChat, ref)
    ).map {
      case GroupsRepositoryService.AlreadyAddedToGroup => FailureProtocol(s"[TGBot] Bot is already in the group[$chatId]")
      case GroupsRepositoryService.RegisterInGroupSuccess => SuccessProtocol(s"[TGBot] Successfully registered bot in new group[$chatId]")
    }
  }

  private def removeBotFromGroup(chatId: ChatId): Future[TelegramBotProtocol] = {
    groupsServiceManager.ask(ref => GroupsRepositoryService.RemoveFromGroup(chatIdToLong(chatId), ref)).map {
      case GroupsRepositoryService.IsNotInGroup => FailureProtocol(s"[TGBot] Bot is not in a group[$chatId]")
      case GroupsRepositoryService.RemoveFromGroupSuccess => SuccessProtocol(s"[TGBot] Successfully removed from group[$chatId]")
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

  onJoinRequest { _ =>
    logger.info("[TGBot] On Join Request")
    Future()
  }


  // TODO handle addition to groups. Current approach works only for chats
  onUpdate { update =>
    val ignore = Future(IgnoreProtocol)
    if (botIDOpt.isDefined) {
      val botID = botIDOpt.get

      val chatMembersInfoOpt = for {
        m <- update.message
        leftChatMember = m.leftChatMember
        newChatMembers = m.newChatMembers
      } yield (m.chat.chatId, leftChatMember, newChatMembers)

      val res: Future[TelegramBotProtocol] = chatMembersInfoOpt match {
        case Some((chatId: ChatId, _, Some(joinedMembers))) =>
          val botJoinedOpt = joinedMembers.find(u => u.isBot && u.id == botID)

          botJoinedOpt match {
            case Some(_) =>
              logger.info(s"[TGBot] Registering bot at group[$chatId]")
              registerBotInGroup(chatId).recoverWith {
                case ex: Throwable => Future(FailureProtocol(s"[TGBot] Could not register bot in new group[$chatId]: $ex"))
              }
            case _ => ignore
          }
        case Some((chatId: ChatId, Some(leftMember), _)) =>
          if (leftMember.isBot && leftMember.id == botID) {
            logger.info(s"[TGBot] Removing bot from a group[$chatId]")
            removeBotFromGroup(chatId).recoverWith {
              case ex: Throwable => Future(FailureProtocol(s"[TGBot] Could not remove from group[$chatId]: $ex"))
            }
          }
          else ignore
        case _ => ignore
      }
      res.map(p => logger.info(p.toString))
    }
    Future(())
  }

  onCommand("start") { implicit msg =>
    logger.info("[TGBot] 'start' command received")
    reply("Hello!").void
  }
}