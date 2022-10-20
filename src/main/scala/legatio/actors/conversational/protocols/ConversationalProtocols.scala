package legatio.actors.conversational.protocols

import akka.actor.typed.ActorRef
import legatio.actors.CommunicationProtocol

trait ConversationalProtocols extends CommunicationProtocol
case class StartCommand(replyTo: ActorRef[CommunicationProtocol]) extends ConversationalProtocols
case class ConversationalCommand(command: String, value: Option[String], replyTo: ActorRef[CommunicationProtocol])
    extends ConversationalProtocols
