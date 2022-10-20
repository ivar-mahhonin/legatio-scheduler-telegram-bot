package legatio.db

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, DispatcherSelector}
import legatio.actors.CommunicationProtocol
import legatio.db.actors.{GroupsRepositoryActor, SchedulesRepositoryActor}

object InitRepositories {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "RepositoriesManagersGuardian")
  implicit val dispatcherConfig: DispatcherSelector = DispatcherSelector.fromConfig("db-repository-actor")
  lazy val schedules: ActorRef[CommunicationProtocol] = system.systemActorOf(SchedulesRepositoryActor(), "SchedulesRepositoryManager", dispatcherConfig)
  lazy val groups: ActorRef[CommunicationProtocol] = system.systemActorOf(GroupsRepositoryActor(), "GroupsRepositoryManager", dispatcherConfig)
}
