package legatio.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, DispatcherSelector}

object RepositoryManagers {
  private val dispatcherConfig = DispatcherSelector.fromConfig("db-repository-actor")

  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.ignore, "RepositoriesGuardian")

  val schedules: ActorRef[SchedulesRepositoryManager.Command] = system.systemActorOf(SchedulesRepositoryManager(), "SchedulesRepositoryManager", dispatcherConfig)
  val groups: ActorRef[GroupsRepositoryManager.Command] = system.systemActorOf(GroupsRepositoryManager(), "GroupsRepositoryManager", dispatcherConfig)
}
