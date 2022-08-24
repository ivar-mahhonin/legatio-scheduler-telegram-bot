package legatio.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, DispatcherSelector}

object RepositoryServices {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "RepositoriesManagersGuardian")
  implicit val dispatcherConfig: DispatcherSelector = DispatcherSelector.fromConfig("db-repository-actor")
  lazy val schedules: ActorRef[SchedulesRepositoryService.Command] = system.systemActorOf(SchedulesRepositoryService(), "SchedulesRepositoryManager", dispatcherConfig)
  lazy val groups: ActorRef[GroupsRepositoryService.Command] = system.systemActorOf(GroupsRepositoryService(), "GroupsRepositoryManager", dispatcherConfig)
}
