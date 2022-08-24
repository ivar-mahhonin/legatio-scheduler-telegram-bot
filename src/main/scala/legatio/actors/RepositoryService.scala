package legatio.actors

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait RepositoryService[T] {
  trait Command
  trait CommandResult extends Command
  final case class Get(id: Long, replyTo: ActorRef[CommandResult]) extends Command
  final case class Delete(id: Long, replyTo: ActorRef[CommandResult]) extends Command
  case class GetSuccess(schedule: T) extends CommandResult
  case class GetAllSuccess(entities: List[T]) extends CommandResult
  case object CreateSuccess extends CommandResult
  case object UpdateSuccess extends CommandResult
  case object DeleteSuccess extends CommandResult
  case class ResultFailure(reason: String) extends CommandResult
  final case class WrappedResult(result: CommandResult, replyTo: ActorRef[CommandResult]) extends Command

  def defaultBehaviour(context: ActorContext[Command]): PartialFunction[Command, Behavior[Command]] = {
    case WrappedResult(result, replyTo) =>
      result match {
        case ResultFailure(ex) => context.log.info(s"[${context.self.path.name}] command failed: [$ex]")
        case _ => context.log.info(s"[${context.self.path.name}] Action completed")
      }
      replyTo ! result
      Behaviors.same
    case _ =>
      context.log.info(s"[${context.self.path.name}] Command not supported")
      Behaviors.same
  }

  protected def wrapFuture(
                            fut: Future[CommandResult],
                            context: ActorContext[Command],
                            replyTo: ActorRef[CommandResult]) = context.pipeToSelf(fut) {
    case Success(r) => WrappedResult(r, replyTo)
    case Failure(ex) => WrappedResult(ResultFailure(ex.toString), replyTo)
  }
}
