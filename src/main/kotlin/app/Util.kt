package app

import com.andreapivetta.kolor.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object Util {

    internal const val DB_URL = "jdbc:h2:~/cashback-helper"

    internal const val DB_URL_TEST = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"

    internal fun printIfSuccess(result: TransactionResult, successMessage: String) {
        when (result) {
            is Success -> println(successMessage)
            is Error -> println(result.message.lightRed())
        }
    }

    internal val greeting = """
        Welcome to ${"Cashback Helper!".lightGreen()}
        Type ${"-h".magenta()} or ${"--help".magenta()} to access the list of available commands.
        Use ${"COMMAND -h".magenta()} or ${"COMMAND --help".magenta()} to learn more about a certain command.
        To exit the application enter ${"quit".magenta()}.
    
        ${"NOTE:".lightYellow()} please surround command argument with quotation marks in case it contains spaces.
    """.trimIndent()

    internal val goodbye = "Until next time!".lightBlue()

    internal val preserveQuotesRegex = Regex("""\s(?=([^"]*"[^"]*")*[^"]*$)""")

    internal fun currentDate(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}