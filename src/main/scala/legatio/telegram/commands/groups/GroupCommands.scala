package legatio.telegram.commands.groups

import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import legatio.telegram.commands.BotCommands
import legatio.telegram.commands.groups.GroupCommands.LIST_GROUPS
import legatio.telegram.MarkDownUtils

trait GroupCommands extends BotCommands { this: TelegramBot with Polling =>
  onCommand(LIST_GROUPS.toString) { implicit msg =>
    logger.info("[TGBot] 'groups' command received")
    for {
      groups <- GroupCommandsService.getAllUserGroups()
      groupNames = groups.map(_.name)
      _ <- request(
        SendMessage(
          msg.chat.chatId,
          "Select group you would like to manage:",
          replyMarkup = Some(MarkDownUtils.createMarkDownChoice(groupNames, "select-group"))
        )
      )
    } yield ()
  }
}
object GroupCommands extends Enumeration {
  val LIST_GROUPS: Value = Value(1, "groups")
}
