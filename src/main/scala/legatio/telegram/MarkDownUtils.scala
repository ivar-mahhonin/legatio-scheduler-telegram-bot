package legatio.telegram

import com.bot4s.telegram.models.{InlineKeyboardButton, InlineKeyboardMarkup}

object MarkDownUtils {
  def createMarkDownChoice(choices: List[String], callBackData: String): InlineKeyboardMarkup = InlineKeyboardMarkup
    .singleColumn(buttonColumn = choices
        .map(choice => InlineKeyboardButton.callbackData(choice, s"$callBackData|$choice"))
        .to(Seq)
    )
}
