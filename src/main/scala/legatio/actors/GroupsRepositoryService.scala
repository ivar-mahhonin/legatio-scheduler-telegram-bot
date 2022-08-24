package legatio.actors

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import legatio.models.Group
import legatio.services.GroupsRepository

import scala.concurrent.Future
import scala.util.{Failure, Success}

// FIXME replace global context with actor specific context
import scala.concurrent.ExecutionContext.Implicits.global

object GroupsRepositoryService extends RepositoryService[Group] {
  case class RegisterInGroup(groupId: Long, isChannel: Boolean = false, isGroup: Boolean = false, replyTo: ActorRef[CommandResult]) extends Command
  case object RegisterInGroupSuccess extends CommandResult
  case object AlreadyAddedToGroup extends CommandResult
  case class RemoveFromGroup(groupId: Long, replyTo: ActorRef[CommandResult]) extends Command
  case object RemoveFromGroupSuccess extends CommandResult
  case object IsNotInGroup extends CommandResult

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.receiveMessagePartial(behaviour(context, context.self.path.name).orElse(defaultBehaviour(context)))
  }

  def behaviour(context: ActorContext[Command], name: String): PartialFunction[Command, Behavior[Command]] = {
    case RegisterInGroup(groupId, isChannel, isGroup, replyTo) =>
      context.log.info(s"[$name] Adding bot to new Chat[$groupId]")

      lazy val findIfBotInGroup = GroupsRepository.findByExternalId(groupId)
      lazy val insertFuture = GroupsRepository.insert(Group(externalId = groupId, isChannel = isChannel, isGroup = isGroup))

      lazy val response = for {
        groupOpt <- findIfBotInGroup
        r <- if (groupOpt.isDefined) Future(AlreadyAddedToGroup) else insertFuture.map(_ => RegisterInGroupSuccess)
      } yield r

      wrapFuture(response,context, replyTo)
      Behaviors.same
    case RemoveFromGroup(groupId, replyTo) =>
      context.log.info(s"[$name] Removing bot from Chat[$groupId]")

      lazy val findIfBotInGroup = GroupsRepository.findByExternalId(groupId)
      lazy val deleteFuture = GroupsRepository.deleteByExternalId(groupId)

      lazy val response = for {
        groupOpt <- findIfBotInGroup
        r <- if (groupOpt.isDefined) deleteFuture.map(_ => RemoveFromGroupSuccess) else Future(IsNotInGroup)
      } yield r

      wrapFuture(response,context, replyTo)
      Behaviors.same
  }

  case class RegisterInGroupSuccess(count: Any)
}

