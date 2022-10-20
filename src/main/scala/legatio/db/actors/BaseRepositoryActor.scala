package legatio.db.actors

import akka.actor.typed.ActorRef
import legatio.actors.{BaseActor, CommunicationProtocol, ResultProtocol}

trait BaseRepositoryActor[T] extends BaseActor {
  final case class Get(id: Long, replyTo: ActorRef[ResultProtocol]) extends CommunicationProtocol
  final case class Delete(id: Long, replyTo: ActorRef[ResultProtocol]) extends CommunicationProtocol
  final case class GetAll(replyTo: ActorRef[ResultProtocol]) extends ResultProtocol

  final case class GetSuccess(schedule: T) extends ResultProtocol
  final case class GetAllSuccess(entities: List[T]) extends ResultProtocol
  case object DeleteSuccess extends ResultProtocol
}
