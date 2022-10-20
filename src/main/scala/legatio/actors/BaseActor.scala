package legatio.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait CommunicationProtocol
final case class WrappedResult[T](result: T, replyTo: ActorRef[T]) extends CommunicationProtocol
final case class FailureProtocol(reason: String) extends ResultProtocol
trait ResultProtocol extends CommunicationProtocol

trait BaseActor {
  def defaultBehaviour(context: ActorContext[CommunicationProtocol]): PartialFunction[CommunicationProtocol, Behavior[CommunicationProtocol]] = {
    case WrappedResult(result: ResultProtocol, replyTo: ActorRef[ResultProtocol]) =>
      result match {
        case FailureProtocol(ex) => context.log.info(s"[${context.self.path.name}] command failed: [$ex]")
        case _ => context.log.info(s"[${context.self.path.name}] Action completed")
      }
      replyTo ! result
      Behaviors.same
    case _ =>
      context.log.info(s"[${context.self.path.name}] Command not supported")
      Behaviors.same
  }

  protected def wrapFuture(
                            fut: Future[ResultProtocol],
                            context: ActorContext[CommunicationProtocol],
                            replyTo: ActorRef[ResultProtocol]): Unit = context.pipeToSelf(fut) {
    case Success(r) => WrappedResult[ResultProtocol](r, replyTo)
    case Failure(ex) => WrappedResult(FailureProtocol(ex.toString), replyTo)
  }
}
