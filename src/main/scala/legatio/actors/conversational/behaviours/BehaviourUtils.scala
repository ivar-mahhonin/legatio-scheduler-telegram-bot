package legatio.actors.conversational.behaviours

import akka.actor.typed.ActorRef
import legatio.actors.CommunicationProtocol
import legatio.actors.conversational.protocols.{ConversationalCommand, ConversationalProtocols, StartCommand};

// TODO string command to command object
case object BehaviourUtils extends ConversationalProtocols {
  def commandStringToCommand(
    command: String,
    value: Option[String],
    replyTo: ActorRef[CommunicationProtocol]
  ): ConversationalProtocols = {
    StartCommand(null)
  }
}
