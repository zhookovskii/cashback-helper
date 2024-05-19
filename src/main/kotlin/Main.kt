import app.*
import com.andreapivetta.kolor.*
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.ExecutionException

private lateinit var service: CashbackService

@Command(
    name = "",
    mixinStandardHelpOptions = true,
    subcommands = [CBHelper.Add::class, CBHelper.Remove::class]
)
class CBHelper : Runnable {

    @Command(
        name = "add",
        mixinStandardHelpOptions = true,
        description = ["Add bank, card, transaction or cashback for current or future month"]
    )
    class Add {

        @Command(name = "bank", mixinStandardHelpOptions = true, description = ["Add new bank"])
        fun bank(
            @Option(names = ["-l", "--limit"], paramLabel = "limit", description = ["Pay limit (optional)"])
            limit: Double?,
            @Parameters(paramLabel = "<name>", description = ["Bank name"])
            name: String,
        ) {
            val message = "Added bank ${name.magenta()} " +
                    if (limit != null) "with pay limit ${limit.toInt().toString().yellow()}" else ""

            Util.printIfSuccess(
                service.addBank(name, limit),
                message
            )
        }

        @Command(name = "card", mixinStandardHelpOptions = true, description = ["Add new card"])
        fun card(
            @Parameters(paramLabel = "<bank name>", description = ["Bank name"])
            bankName: String,
            @Parameters(paramLabel = "<card name>", description = ["Card name"])
            cardName: String
        ) {
            val message = "Added card ${cardName.lightGreen()} for bank ${bankName.magenta()}"

            Util.printIfSuccess(
                service.addCard(bankName, cardName),
                message
            )
        }

        @Command(
            name = "cashback",
            mixinStandardHelpOptions = true,
            description = ["Add cashback category for current or future month"]
        )
        fun cashback(
            @Parameters(paramLabel = "<period>", description = ["Cashback period (current or future)"])
            period: String,
            @Parameters(paramLabel = "<card name>", description = ["Card name"])
            cardName: String,
            @Parameters(paramLabel = "<category>", description = ["Cashback category"])
            category: String,
            @Parameters(paramLabel = "<percent>", description = ["Cashback percent"])
            percent: Double,
            @Option(
                names = ["-p", "--permanent"],
                paramLabel = "permanent",
                description = ["Flag that indicates that the category is permanent"]
            )
            permanent: Boolean
        ) {
            if (period != "current" && period != "future") {
                println("Invalid period: $period, cashback period can only be current or future".lightRed())
                return
            }
            val message = "Added category ${category.lightCyan()} with " +
                    "${(percent.toString() + "%").lightYellow()} cashback " +
                    "for card ${cardName.lightGreen()} " +
                    "in $period month ${if (permanent) "permanently" else ""}"

            Util.printIfSuccess(
                service.addCashback(period, cardName, category, percent, permanent),
                message
            )
        }

        @Command(
            name = "transaction",
            mixinStandardHelpOptions = true,
            description = ["Add transaction"]
        )
        fun transaction(
            @Parameters(paramLabel = "<card name>", description = ["Card name"])
            cardName: String,
            @Parameters(paramLabel = "<category>", description = ["Transaction category"])
            category: String,
            @Parameters(paramLabel = "<value>", description = ["Transaction value"])
            value: Double
        ) {
            val message = "Added ${value.toInt().toString().yellow()} transaction " +
                    "from ${cardName.lightGreen()} (${category.lightCyan()})"

            Util.printIfSuccess(
                service.addTransaction(cardName, category, value),
                message
            )
        }
    }

    @Command(
        name = "remove",
        mixinStandardHelpOptions = true,
        description = ["Remove cashback for current or future month"]
    )
    class Remove {

        @Command(
            name = "cashback",
            mixinStandardHelpOptions = true,
            description = ["Remove cashback for current or future month"]
        )
        fun cashback(
            @Parameters(paramLabel = "<period>", description = ["Cashback period (current or future)"])
            period: String,
            @Parameters(paramLabel = "<card name>", description = ["Card name"])
            cardName: String,
            @Parameters(paramLabel = "<category>", description = ["Cashback category"])
            category: String
        ) {
            val message = "Removed cashback category ${category.lightCyan()} " +
                    "for card ${cardName.lightGreen()} in $period month"

            Util.printIfSuccess(
                service.removeCashback(period, cardName, category),
                message
            )
        }
    }

    @Command(
        name = "list",
        mixinStandardHelpOptions = true,
        description = ["List cards with cashback in current month"]
    )
    fun list() {
        val cards = service.listCards()
        if (cards.isNotEmpty()) {
            println("Cards with active cashbacks:")
            println(
                cards.entries.joinToString("\n") { entry ->
                    val cashbacks = entry.value.joinToString(", ") {
                        "${it.first.lightCyan()} (${(it.second.toString() + "%").lightYellow()})"
                    }
                    "Card: ${entry.key.lightGreen()}    Categories: $cashbacks"
                }
            )
        } else {
            println("There are no cards with active cashbacks at the moment. Try and add some!")
        }
    }

    @Command(
        name = "choose",
        mixinStandardHelpOptions = true,
        description = ["Choose a card with highest cashback for given category"]
    )
    fun choose(
        @Parameters(paramLabel = "<category>", description = ["Transaction category"])
        category: String,
        @Option(names = ["-v", "-value"], paramLabel = "value", description = ["Transaction value (optional)"])
        value: Double?
    ) {
        val card = service.chooseCard(category, value)
        if (card != null) {
            println("Use card ${card.lightGreen()} for this purchase, it's the best one")
        } else {
            println("Any card works for this purchase")
        }
    }

    @Command(
        name = "estimate",
        mixinStandardHelpOptions = true,
        description = ["Estimate cashback for current month"]
    )
    fun estimate() {
        val cashbacks = service.estimateCashback()
        if (cashbacks.isNotEmpty()) {
            println("Cashbacks for this month:")
            println(cashbacks.joinToString("\n") {
                "Card: ${it.first.lightGreen()}    Cashback: ${it.second.toInt().toString().yellow()}"
            })
        } else {
            println("No cashback at the moment: add some cards, cashbacks or transactions!")
        }
    }

    override fun run() {
        println(Util.greeting)
        while (true) {
            val input = readlnOrNull()?.trim() ?: continue
            if (input.equals("quit", ignoreCase = true)) {
                println(Util.goodbye)
                break
            } else {
                val args = input
                    .split(Util.preserveQuotesRegex)
                    .map {
                        if (it.isNotBlank() && it.first() == '"' && it.last() == '"')  {
                            it.substring(1, it.length - 1)
                        } else {
                            it
                        }
                    }.toTypedArray()
                try {
                    CommandLine(CBHelper()).execute(*args)
                } catch (e: ExecutionException) {
                    println(e.message)
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    service = CashbackService(Util.DB_URL)
    CommandLine(CBHelper()).execute(*args)
}
