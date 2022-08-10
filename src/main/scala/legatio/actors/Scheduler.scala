package legatio.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import legatio.db.services.SchedulesRepository
import legatio.models.Schedule
import slogging.{LazyLogging, LogLevel, LoggerConfig, PrintLoggerFactory}

import java.time.Instant
import scala.concurrent.Future
import scala.util.{Success, Failure}

object Scheduler extends LazyLogging {
  sealed trait Command
  sealed trait CommandResult  extends Command
  case class ResultFailure(reason: String) extends CommandResult

  private final case class WrappedResult(result: CommandResult, replyTo: ActorRef[CommandResult]) extends Command

  final case class Get(id: Long, replyTo: ActorRef[CommandResult]) extends Command
  case class GetSuccess(schedule: Schedule) extends CommandResult


  final case class GetAll(replyTo: ActorRef[CommandResult]) extends Command
  case class GetAllSuccess(schedules: List[Schedule]) extends CommandResult

  final case class Create(message: String, date: String, replyTo: ActorRef[CommandResult]) extends Command
  case object CreateSuccess extends CommandResult


  final case class Update(id: Long, message: String, date: String, replyTo: ActorRef[CommandResult]) extends Command
  case object UpdateSuccess extends CommandResult


  final case class Delete(id: Long, replyTo: ActorRef[CommandResult]) extends Command
  case object DeleteSuccess extends CommandResult


  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  def apply(): Behavior[Command] = Behaviors.receive { (context, message) =>
    message match {
      case Get(id, replyTo) =>
        context.log.info(s"[Scheduler] Querying one schedule")
        val oneScheduleFuture: Future[Option[Schedule]] = SchedulesRepository.getById(id)
        context.pipeToSelf(oneScheduleFuture) {
          case Success(value) => value match {
            case Some(value) => WrappedResult(GetSuccess(value), replyTo)
            case None => WrappedResult(ResultFailure(s"Schedule by id[$id] is missing"), replyTo)
          }
          case Failure(ex) => WrappedResult(ResultFailure(ex.toString), replyTo)
        }
        Behaviors.same
      case GetAll(replyTo) =>
        context.log.info(s"[Scheduler] Querying all schedules")
        val listFuture = SchedulesRepository.list()
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
        Behaviors.same
      case Delete(id, replyTo) =>
        context.log.info(s"[Scheduler] Canceling existing schedule[$id]")
        val deleteFuture = SchedulesRepository.delete(id)
        context.pipeToSelf(deleteFuture) {
          case Success(_) => WrappedResult(DeleteSuccess, replyTo)
          case Failure(ex) => WrappedResult(ResultFailure(ex.toString), replyTo)
        }
        Behaviors.same
      case WrappedResult(result, replyTo) =>
        result match {
          case ResultFailure(ex) => context.log.info(s"[Scheduler] command failed: [$ex]")
          case _ => context.log.info(s"[Scheduler] Action completed")
        }
        replyTo ! result
        Behaviors.same
      case _ =>
        context.log.info(s"[Scheduler] Command not supported")
        Behaviors.same
    }
  }

  private def stringDateToEpoch(date: String) = Instant.parse(date).toEpochMilli
}

