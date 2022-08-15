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

class TGBot(token: String) extends TelegramBot with Polling with Commands[Future] with Updates[Future] with JoinRequests[Future] {
  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  override val client: RequestHandler[Future] = new ScalajHttpClient(token)
  implicit val timeout: Timeout = 3.seconds

  private var botIDOpt: Option[Long] = None
  private val groups = RepositoryServices.groups
  private val schedules = RepositoryServices.schedules


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

  private def chatIdToLong(chatId: ChatId) = chatId.toEither match {
    case Left(l) => l
    case Right(s) => s.toLong
  }

  private def registerBotInGroup(chatId: ChatId): Unit = {
    println(s"[TGBot] Registering bot at group[$chatId]")
    val registerFuture = groups.ask(ref => GroupsRepositoryService.RegisterInGroup(
      chatIdToLong(chatId), chatId.isChannel, chatId.isChat, ref)
    )
    registerFuture.onComplete {
      case Success(GroupsRepositoryService.RegisterInGroupSuccess) => println(s"[TGBot] Successfully registered bot in new group[$chatId]")
      case Failure(ex) => println(s"[TGBot] Could not register bot in new group[$chatId]: $ex")
    }
  }

  private def removeBotFromGroup(chatId: ChatId, isBot: Boolean, leftMemberId: Long, botID: Long): Unit = {
    if (isBot && leftMemberId == botID) {
      val removeFuture = RepositoryServices.groups.ask(ref => GroupsRepositoryService.RemoveFromGroup(chatIdToLong(chatId), ref))
      removeFuture.onComplete {
        case Success(GroupsRepositoryService.RemoveFromGroupSuccess) => println(s"[TGBot] Successfully removed from group[$chatId]")
        case Failure(ex) => println(s"[TGBot] Could not remove from group[$chatId]: $ex")
      }
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

  onJoinRequest { req =>
    println("On Join Request")
    Future()
  }


  // TODO handle addition to groups. Current approach works only for chats
  onUpdate { update =>
    if (botIDOpt.isDefined) {
      val botID = botIDOpt.get

      val chatMembersInfoOpt = for {
        m <- update.message
        leftChatMember = m.leftChatMember
        newChatMembers = m.newChatMembers
      } yield (m.chat.chatId, leftChatMember, newChatMembers)

      chatMembersInfoOpt match {
        case Some((chatId: ChatId, _, Some(joinedMembers))) =>
          val botJoinedOpt = joinedMembers.find(u => u.isBot && u.id == botID)
          botJoinedOpt match {
            case Some(_) => registerBotInGroup(chatId)
            case _ =>
          }
        case Some((chatId: ChatId, Some(leftMember), _)) => removeBotFromGroup(chatId, leftMember.isBot, leftMember.id, botID)
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