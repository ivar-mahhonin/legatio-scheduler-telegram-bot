package legatio.actors.conversational.behaviours.welcome

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import legatio.actors.CommunicationProtocol
import legatio.actors.conversational.protocols.{ConversationalProtocols, StartCommand}

case object WelcomeBehavior {
  def apply(): PartialFunction[CommunicationProtocol, Behavior[CommunicationProtocol]] = {
    case StartCommand(replyTo) =>
      Behaviors.same
  }
}

