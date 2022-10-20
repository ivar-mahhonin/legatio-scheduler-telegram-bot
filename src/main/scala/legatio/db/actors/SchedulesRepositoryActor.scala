package legatio.db.actors

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import legatio.actors.{CommunicationProtocol, ResultProtocol, WrappedResult, FailureProtocol}
import legatio.models.Schedule
import legatio.services.SchedulesRepository

import java.time.Instant
import scala.util.{Failure, Success}

object SchedulesRepositoryActor extends BaseRepositoryActor[Schedule] {
  // final case class GetAll(userId: String, chatId: String, replyTo: ActorRef[CommandResult]) extends Command

  final case class Create(message: String, date: String, replyTo: ActorRef[ResultProtocol]) extends CommunicationProtocol

  final case class Update(id: Long, message: String, date: String, replyTo: ActorRef[ResultProtocol]) extends CommunicationProtocol

  def apply(): Behavior[CommunicationProtocol] = Behaviors.setup { context =>
    Behaviors.receiveMessagePartial(behaviour(context, context.self.path.name).orElse(defaultBehaviour(context)))
  }

  def behaviour(context: ActorContext[CommunicationProtocol], name: String): PartialFunction[CommunicationProtocol, Behavior[CommunicationProtocol]] = {
    case Get(id, replyTo) =>
      context.log.info(s"[$name] Querying one schedule")
      context.pipeToSelf(SchedulesRepository.getById(id)) {
        case Success(value) => value match {
          case Some(value) => WrappedResult(GetSuccess(value), replyTo)
          case None => WrappedResult(FailureProtocol(s"[$name] Schedule by id[$id] is missing"), replyTo)
        }
        case Failure(ex) => WrappedResult(FailureProtocol(ex.toString), replyTo)
      }
      Behaviors.same
    /*      case GetAll(replyTo) =>
            context.log.info(s"[Scheduler] Querying all schedules")
            val listFuture = SchedulesRepository.listAll()
            context.pipeToSelf(listFuture) {
              case Success(value) => WrappedResult(GetAllSuccess(value.toList), replyTo)
              case Failure(ex) => WrappedResult(ResultFailure(ex.toString), replyTo)
            }
            Behaviors.same
          case Create(text, date, replyTo) =>
            context.log.info(s"[Scheduler] Creating new schedule at $date")
            val insertFuture = SchedulesRepository.insert(Schedule(text = text, date = stringDateToEpoch(date)))
            context.pipeToSelf(insertFuture) {
              case Success(_) => WrappedResult(CreateSuccess, replyTo)
              case Failure(ex) => WrappedResult(ResultFailure(ex.toString), replyTo)
            }
            Behaviors.same
          case Update(id, message, date, replyTo) =>
            context.log.info(s"[Scheduler] Updating existing schedule[$id]")
            val updateFuture = SchedulesRepository.update(id, Schedule(text = message, date = stringDateToEpoch(date)))
            context.pipeToSelf(updateFuture) {
              case Success(_) => WrappedResult(UpdateSuccess, replyTo)
              case Failure(ex) => WrappedResult(ResultFailure(ex.toString), replyTo)
            }
            Behaviors.same*/
    case Delete(id, replyTo) =>
      context.log.info(s"[$name] Canceling existing schedule[$id]")
      val deleteFuture = SchedulesRepository.delete(id)
      context.pipeToSelf(deleteFuture) {
        case Success(_) => WrappedResult(DeleteSuccess, replyTo)
        case Failure(ex) => WrappedResult(FailureProtocol(ex.toString), replyTo)
      }
      Behaviors.same
  }

  private def stringDateToEpoch(date: String) = Instant.parse(date).toEpochMilli
}
