package legatio.db.actors

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import legatio.actors.{CommunicationProtocol, ResultProtocol}
import legatio.models.Group
import legatio.services.GroupsRepository

import scala.concurrent.Future
// FIXME replace global context with actor specific context
import scala.concurrent.ExecutionContext.Implicits.global

object GroupsRepositoryActor extends BaseRepositoryActor[Group] {
  final case class RegisterInGroup(groupId: Long, name: String, isChannel: Boolean = false, replyTo: ActorRef[ResultProtocol]) extends CommunicationProtocol

  case object RegisterInGroupSuccess extends ResultProtocol

  case object AlreadyAddedToGroup extends ResultProtocol

  final case class RemoveFromGroup(groupId: Long, replyTo: ActorRef[ResultProtocol]) extends CommunicationProtocol

  case object RemoveFromGroupSuccess extends ResultProtocol

  case object IsNotInGroup extends ResultProtocol

  def apply(): Behavior[CommunicationProtocol] = Behaviors.setup { context =>
    Behaviors.receiveMessagePartial(behaviour(context, context.self.path.name).orElse(defaultBehaviour(context)))
  }

  def behaviour(context: ActorContext[CommunicationProtocol], name: String): PartialFunction[CommunicationProtocol, Behavior[CommunicationProtocol]] = {
    case GetAll(replyTo) =>
      context.log.info(s"[Scheduler] Get All Groups")
      val getAllFuture = GroupsRepository.getAll().map(groups => GetAllSuccess(groups.toList))
      wrapFuture(getAllFuture, context, replyTo)
      Behaviors.same
    case RegisterInGroup(groupId, name, isChannel, replyTo) =>
      context.log.info(s"[$name] Adding bot to new Chat[$groupId]")

      lazy val findIfBotInGroup = GroupsRepository.findByExternalId(groupId)
      lazy val insertFuture = GroupsRepository.insert(Group(name, groupId, isChannel))

      lazy val response = for {
        groupOpt <- findIfBotInGroup
        r <- if (groupOpt.isDefined) Future(AlreadyAddedToGroup) else insertFuture.map(_ => RegisterInGroupSuccess)
      } yield r

      wrapFuture(response, context, replyTo)
      Behaviors.same
    case RemoveFromGroup(groupId, replyTo) =>
      context.log.info(s"[$name] Removing bot from Chat[$groupId]")

      lazy val findIfBotInGroup = GroupsRepository.findByExternalId(groupId)
      lazy val deleteFuture = GroupsRepository.deleteByExternalId(groupId)

      lazy val response = for {
        groupOpt <- findIfBotInGroup
        r <- if (groupOpt.isDefined) deleteFuture.map(_ => RemoveFromGroupSuccess) else Future(IsNotInGroup)
      } yield r

      wrapFuture(response, context, replyTo)
      Behaviors.same
  }

  final case class RegisterInGroupSuccess(count: Any)
}
