package app

import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class CashbackService(dbURL: String) {

    private fun updateCashback() {
        val periodChanged = { date: LocalDate ->
            date.month != Util.currentDate().month || date.year != Util.currentDate().year
        }
        transaction {
            Cashback.all().filter { periodChanged(it.date)  }.forEach {
                when (it.period) {
                    "current" -> {
                        if (!it.permanent) it.delete()
                    }
                    "future" -> {
                        it.period = "current"
                        it.date = Util.currentDate()
                    }
                }
            }
            Card.all().filter { periodChanged(it.date)  }.forEach {
                it.cashback = 0.0
                it.date = Util.currentDate()
            }
            Bank.all().filter { periodChanged(it.date)  }.forEach {
                it.pay = 0.0
                it.date = Util.currentDate()
            }
        }
    }

    init {
        Database.connect(
            url = dbURL,
            driver = "org.h2.Driver",
            user = "root",
            password = ""
        )
        transaction {
            SchemaUtils.create(Banks, Cards, Cashbacks)
        }
    }

    fun addBank(bankName: String, bankLimit: Double?): TransactionResult {
        return transaction {
            val bank = Bank.find { Banks.name eq bankName }
            if (!bank.empty()) {
                return@transaction Error("The bank $bankName already exists")
            }

            Bank.new {
                name = bankName
                limit = bankLimit
                pay = 0.0
                date = Util.currentDate()
            }
            return@transaction Success
        }
    }

    fun addCard(bankName: String, cardName: String): TransactionResult {
        return transaction {
            if (Bank.find { Banks.name eq bankName }.empty()) {
                return@transaction Error("Unknown bank: $bankName")
            }

            if (!Card.find { Cards.name eq cardName }.empty()) {
                return@transaction Error("The card $cardName already exists")
            }

            Card.new {
                name = cardName
                bank = bankName
                cashback = 0.0
                date = Util.currentDate()
            }
            return@transaction Success
        }
    }

    fun addCashback(
        cbPeriod: String,
        cardName: String,
        cbCategory: String,
        cbPercent: Double,
        cbPermanent: Boolean
    ): TransactionResult {
        return transaction {
            if (Card.find { Cards.name eq cardName }.empty()) {
                return@transaction Error("Unknown card: $cardName")
            }

            Cashback.findSingleByAndUpdate(
                Op.build {
                    Cashbacks.period eq cbPeriod and
                            (Cashbacks.card eq cardName) and
                            (Cashbacks.category eq cbCategory)
                }
            ) {
                it.percent = cbPercent
                it.permanent = cbPermanent
                it.date = Util.currentDate()
            } ?: Cashback.new {
                period = cbPeriod
                card = cardName
                category = cbCategory
                percent = cbPercent
                permanent = cbPermanent
                date = Util.currentDate()
            }
            return@transaction Success
        }
    }

    fun addTransaction(cardName: String, tranCategory: String, tranValue: Double): TransactionResult {
        updateCashback()
        return transaction {
            val card = Card.find {
                Cards.name eq cardName
            }.singleOrNull() ?: return@transaction Error("Unknown card: $cardName")

            val cbCard = Cashback.find {
                Cashbacks.category eq tranCategory and
                        (Cashbacks.card eq cardName) and
                        (Cashbacks.period eq "current")
            }.singleOrNull() ?: return@transaction Success

            val bank = Bank.find { Banks.name eq card.bank }.single()

            val cashback = tranValue / 100 * cbCard.percent

            if (bank.limit == null) {
                card.cashback += cashback
                bank.pay += cashback
                return@transaction Success
            }

            if (bank.pay < bank.limit!!) {
                if (bank.pay + cashback < bank.limit!!) {
                    card.cashback += cashback
                    bank.pay += cashback
                } else {
                    card.cashback += (bank.limit!! - bank.pay)
                    bank.pay = bank.limit!!
                }
            }

            return@transaction Success
        }
    }

    fun removeCashback(period: String, cardName: String, category: String): TransactionResult {
        return transaction {
            val cashback = Cashback.find {
                Cashbacks.period eq period and
                        (Cashbacks.card eq cardName) and
                        (Cashbacks.category eq category)
            }.singleOrNull() ?: return@transaction Error("No such cashback!")
            cashback.delete()
            return@transaction Success
        }
    }

    fun listCards(): Map<String, List<Pair<String, Double>>> {
        updateCashback()
        return transaction {
            Cashback.find {
                Cashbacks.period eq "current"
            }.groupBy {
                it.card
            }.mapValues {
                it.value.map { cb -> cb.category to cb.percent }
            }
        }
    }

    fun chooseCard(category: String, value: Double?): String? {
        updateCashback()
        return transaction {
            val cashbacks = Cashback.find { Cashbacks.category eq category and (Cashbacks.period eq "current") }
            value ?: return@transaction cashbacks.maxByOrNull { it.percent }?.card

            cashbacks.maxByOrNull {
                val card = Card.find { Cards.name eq it.card }.single()
                val bank = Bank.find { Banks.name eq card.bank }.single()
                if (bank.limit == null) {
                    it.percent / 100 * value
                } else if (bank.pay == bank.limit!!) {
                    0.0
                } else if (bank.pay + it.percent / 100 * value < bank.limit!!) {
                    it.percent / 100 * value
                } else {
                    bank.limit!! - bank.pay
                }
            }?.card
        }
    }

    fun estimateCashback(): List<Pair<String, Double>> {
        updateCashback()
        return transaction {
            Card.find { Cards.cashback greater 0.0 }.map { it.name to it.cashback }
        }
    }

}