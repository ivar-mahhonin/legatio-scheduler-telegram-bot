package legatio.telegram.commands.groups

import akka.util.Timeout
import com.bot4s.telegram.models.{ChatId, User}
import legatio.actors.FailureProtocol
import legatio.db.InitRepositories
import legatio.db.actors.GroupsRepositoryActor
import legatio.models.Group
import legatio.telegram.TelegramBotProtocol.{TelegramFailureProtocol, TelegramSuccessProtocol}
import legatio.telegram.commands.BotCommands
import legatio.telegram.TelegramBotProtocol
import slick.util.Logging

object GroupCommandsService extends Logging {
  import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
  import legatio.db.InitRepositories.system

  import scala.concurrent.duration._
  import scala.concurrent.{ExecutionContext, Future}

  private val groups = InitRepositories.groups
  implicit val timeout: Timeout = 3.seconds

  def getAllUserGroups()(implicit executor: ExecutionContext): Future[List[Group]] =
    groups.ask(ref => GroupsRepositoryActor.GetAll(ref)).map {
      case FailureProtocol(_)      => List()
      case GroupsRepositoryActor.GetAllSuccess(entities) => entities
    }

  def removeBotFromGroup(chatId: ChatId)(implicit executor: ExecutionContext): Future[TelegramBotProtocol] =
    groups.ask(ref => GroupsRepositoryActor.RemoveFromGroup(BotCommands.chatIdToLong(chatId), ref)).map {
      case GroupsRepositoryActor.IsNotInGroup => TelegramFailureProtocol(s"[TGBot] Bot is not in a group[$chatId]")
      case GroupsRepositoryActor.RemoveFromGroupSuccess =>
        TelegramSuccessProtocol(s"[TGBot] Successfully removed from group[$chatId]")
    }

  def registerBotInGroup(title: String, chatId: ChatId)(
    implicit executor: ExecutionContext
  ): Future[TelegramBotProtocol] =
    groups
      .ask(ref => GroupsRepositoryActor.RegisterInGroup(BotCommands.chatIdToLong(chatId), title, chatId.isChannel, ref))
      .map {
        case GroupsRepositoryActor.AlreadyAddedToGroup =>
          TelegramFailureProtocol(s"[TGBot] Bot is already in the group[$chatId]")
        case GroupsRepositoryActor.RegisterInGroupSuccess =>
          TelegramSuccessProtocol(s"[TGBot] Successfully registered bot in new group[$chatId]")
      }

  def ifBotJoinedGroup(botID: Long, chatTitle: String, chatId: ChatId, joinedMembers: Array[User])(
    implicit executor: ExecutionContext
  ): Future[TelegramBotProtocol] = {
    joinedMembers.find(u => u.isBot && u.id == botID) match {
      case Some(_) =>
        logger.info(s"[TGBot] Registering bot at group [$chatTitle] [$chatId]")
        registerBotInGroup(chatTitle, chatId).recoverWith {
          case ex: Throwable => Future(TelegramFailureProtocol(s"[TGBot] Could not register bot in new group[$chatId]: $ex"))
        }
      case _ => BotCommands.makeIgnoreProtocolMessage("Could not obtain info, if bot jointed group")
    }
  }

  def ifBotLeftGroup(botID: Long, chatId: ChatId, leftMember: User)(
    implicit executor: ExecutionContext
  ): Future[TelegramBotProtocol] = {
    if (leftMember.isBot && leftMember.id == botID) {
      logger.info(s"[TGBot] Removing bot from a group[$chatId]")
      removeBotFromGroup(chatId).recoverWith {
        case ex: Throwable => Future(TelegramFailureProtocol(s"[TGBot] Could not remove from group[$chatId]: $ex"))
      }
    } else BotCommands.makeIgnoreProtocolMessage("Could not obtain info, if bot left group")
  }
}
