package legatio.telegram

sealed trait TelegramBotProtocol

object TelegramBotProtocol {
  final case class TelegramSuccessProtocol(msg: String) extends TelegramBotProtocol
  final case class TelegramIgnoreProtocol(msg: String) extends TelegramBotProtocol
  final case class TelegramFailureProtocol(msg: String) extends TelegramBotProtocol
}
