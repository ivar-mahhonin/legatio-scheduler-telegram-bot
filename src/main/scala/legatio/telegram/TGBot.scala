package legatio.telegram

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.util.Timeout
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.typesafe.config.ConfigFactory
import legatio.actors.Scheduler
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}
import akka.actor.typed.scaladsl.AskPattern._

import scala.concurrent.duration._
import scala.concurrent.Future

class TGBot(val token: String) extends TelegramBot with Polling with Commands[Future] {
  private val scheduler = ActorSystem(Scheduler(), "Scheduler", ConfigFactory.load().getConfig("db-repository-actor"))

  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  override val client: RequestHandler[Future] = new ScalajHttpClient(token)
  implicit val timeout: Timeout = 3.seconds
  implicit val system: ActorSystem[_] = scheduler


  onCommand("start") { implicit msg =>
    logger.info("[TGBot] 'start' command received")
    reply("Hello!").void
  }

  onCommand("list") { implicit msg =>
    logger.info("[TGBot] 'list' command received")

    scheduler.ask(ref => Scheduler.GetAll(ref)).flatMap {
      case Scheduler.GetAllSuccess(schedules) => reply(s"Found: ${schedules.size} schedules").void
      case Scheduler.ResultFailure(_) => reply(s"Could not retrieve schedules").void
    }
  }
}