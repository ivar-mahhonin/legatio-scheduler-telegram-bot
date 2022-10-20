package legatio.actors.conversational.behaviours.groups

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import legatio.actors.CommunicationProtocol
import legatio.actors.conversational.protocols.{ConversationalProtocols, StartCommand}

case object GroupsBehavior {
  def apply(): PartialFunction[CommunicationProtocol, Behavior[CommunicationProtocol]] = {
    case StartCommand(replyTo) =>
      Behaviors.same
  }
}
