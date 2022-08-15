package legatio.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, DispatcherSelector}

object RepositoryServices {
  private val dispatcherConfig = DispatcherSelector.fromConfig("db-repository-actor")
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "RepositoriesManagersGuardian")

  lazy val schedules: ActorRef[SchedulesRepositoryService.Command] = system.systemActorOf(SchedulesRepositoryService(), "SchedulesRepositoryManager", dispatcherConfig)
  lazy val groups: ActorRef[GroupsRepositoryService.Command] = system.systemActorOf(GroupsRepositoryService(), "GroupsRepositoryManager", dispatcherConfig)
}
