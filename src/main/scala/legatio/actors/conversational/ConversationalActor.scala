package legatio.actors.conversational

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import legatio.actors.conversational.behaviours.groups.GroupsBehavior
import legatio.actors.conversational.behaviours.schedules.SchedulesBehavior
import legatio.actors.conversational.behaviours.welcome.WelcomeBehavior
import legatio.actors.conversational.protocols.ConversationalProtocols
import legatio.actors.{BaseActor, CommunicationProtocol}

class ConversationalActor extends BaseActor with ConversationalProtocols {
  def apply(): Behavior[CommunicationProtocol] = Behaviors.setup { context =>
    Behaviors.receiveMessagePartial(
      WelcomeBehavior()
        .orElse(SchedulesBehavior())
        .orElse(GroupsBehavior())
        .orElse(defaultBehaviour(context))
    )
  }
}
