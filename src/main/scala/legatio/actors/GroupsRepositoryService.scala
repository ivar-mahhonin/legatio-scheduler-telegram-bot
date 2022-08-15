package legatio.actors

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import legatio.models.Group
import legatio.services.GroupsRepository
import scala.util.{Failure, Success}

object GroupsRepositoryService extends RepositoryService[Group] {
  case class RegisterInGroup(groupId: Long, isChannel: Boolean = false, isGroup: Boolean = false, replyTo: ActorRef[CommandResult]) extends Command
  case object RegisterInGroupSuccess extends CommandResult
  case class RemoveFromGroup(groupId: Long, replyTo: ActorRef[CommandResult]) extends Command
  case object RemoveFromGroupSuccess extends CommandResult

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.receiveMessagePartial(behaviour(context, context.self.path.name).orElse(defaultBehaviour(context)))
  }

  def behaviour(context: ActorContext[Command], name: String): PartialFunction[Command, Behavior[Command]] = {
    case RegisterInGroup(groupId, isChannel, isGroup, replyTo) =>
      context.log.info(s"[$name] Adding bot to new Chat[$groupId]")
      val insertFuture = GroupsRepository.insert(Group(externalId = groupId, isChannel = isChannel, isGroup = isGroup))
      context.pipeToSelf(insertFuture) {
        case Success(_) => WrappedResult(RegisterInGroupSuccess, replyTo)
        case Failure(ex) => WrappedResult(ResultFailure(ex.toString), replyTo)
      }
      Behaviors.same
    case RemoveFromGroup(groupId, replyTo) =>
      context.log.info(s"[$name] Removing bot from Chat[$groupId]")
      val deleteFuture = GroupsRepository.deleteByExternalId(groupId)
      context.pipeToSelf(deleteFuture) {
        case Success(_) => WrappedResult(RemoveFromGroupSuccess, replyTo)
        case Failure(ex) => WrappedResult(ResultFailure(ex.toString), replyTo)
      }
      Behaviors.same
  }

  case class RegisterInGroupSuccess(count: Any)
}

