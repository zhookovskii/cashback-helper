import app.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation

@TestMethodOrder(OrderAnnotation::class)
class CashbackServiceTest {

    companion object {
        private lateinit var service: CashbackService

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            service = CashbackService(Util.DB_URL_TEST)
        }
    }

    @Test
    @Order(1)
    fun addsBank() {
        service.addBank("Sberbank", null)
        service.addBank("Tinkoff", 2000.0)
        service.addBank("Alpha", null)
        service.addBank("Sberbank", null)

        transaction {
            val banks = Bank.all()
            Assertions.assertEquals(3, banks.count())
            Assertions.assertEquals(
                setOf("Sberbank", "Tinkoff", "Alpha"),
                banks.map { it.name }.toSet()
            )
        }
    }

    @Test
    @Order(2)
    fun addsCard() {
        service.addCard("Sberbank", "MIR")
        service.addCard("Sberbank", "Sberbank Credit")
        service.addCard("Tinkoff", "Black")
        service.addCard("Tinkoff", "Tinkoff Credit")
        service.addCard("Alpha", "Only")
        service.addCard("Alpha", "Alpha Credit")
        service.addCard("Alpha", "Only")

        transaction {
            val cards = Card.all()
            Assertions.assertEquals(6, cards.count())
            Assertions.assertEquals(
                setOf(
                    "Sberbank" to "MIR",
                    "Sberbank" to "Sberbank Credit",
                    "Tinkoff" to "Black",
                    "Tinkoff" to "Tinkoff Credit",
                    "Alpha" to "Only",
                    "Alpha" to "Alpha Credit"
                ),
                cards.map { it.bank to it.name }.toSet()
            )
        }
    }

    @Test
    @Order(3)
    fun addsCashback() {
        service.addCashback(
            "current",
            "MIR",
            "Food",
            5.0,
            false
        )
        service.addCashback(
            "current",
            "Sberbank Credit",
            "Travel",
            6.0,
            false
        )
        service.addCashback(
            "future",
            "Black",
            "Food",
            8.0,
            true
        )
        service.addCashback(
            "current",
            "Tinkoff Credit",
            "Travel",
            3.0,
            false
        )
        service.addCashback(
            "current",
            "Tinkoff Credit",
            "Pharmacies",
            7.0,
            true
        )
        service.addCashback(
            "current",
            "Only",
            "Pharmacies",
            3.0,
            false
        )
        service.addCashback(
            "current",
            "Alpha Credit",
            "Other",
            1.0,
            true
        )

         transaction {
             val cashbacks = Cashback.all()
             Assertions.assertEquals(7, cashbacks.count())
        }
    }

    @Test
    @Order(4)
    fun addsTransaction() {
        service.addTransaction("MIR", "Travel", 23000.0)
        service.addTransaction("Sberbank Credit", "Food", 1300.0)
        service.addTransaction("Black", "Food", 799.0)
        service.addTransaction("Tinkoff Credit", "Pharmacies", 10000.0)
        service.addTransaction("Only", "Other", 1800.0)
        service.addTransaction("Alpha Credit", "Other", 2100.0)

        transaction {
            val cashbacks = Card.find { Cards.cashback greater 0.0 }
            Assertions.assertEquals(2, cashbacks.count())
            Assertions.assertEquals(
                setOf("Tinkoff Credit" to 700.0, "Alpha Credit" to 21.0),
                cashbacks.map { it.name to it.cashback }.toSet()
            )
        }
    }

    @Test
    @Order(5)
    fun listsCardsWithCashback() {
        val cards = service.listCards()
        Assertions.assertEquals(5, cards.size)
        Assertions.assertEquals(
            mapOf(
                "MIR" to listOf("Food" to 5.0),
                "Sberbank Credit" to listOf("Travel" to 6.0),
                "Tinkoff Credit" to listOf("Travel" to 3.0, "Pharmacies" to 7.0),
                "Only" to listOf("Pharmacies" to 3.0),
                "Alpha Credit" to listOf("Other" to 1.0)
            ),
            cards
        )
    }

    @Test
    @Order(6)
    fun choosesCorrectCard() {
        Assertions.assertEquals(
            "Sberbank Credit",
            service.chooseCard("Travel", null)
        )
        Assertions.assertEquals(
            "MIR",
            service.chooseCard("Food", null)
        )
        // has the highest cashback percent
        Assertions.assertEquals(
            "Tinkoff Credit",
            service.chooseCard("Pharmacies", 20000.0)
        )
        // overwhelm the bank pay limit
        service.addTransaction("Tinkoff Credit", "Pharmacies", 20000.0)
        // should choose another card due to Tinkoff pay limit being exhausted
        Assertions.assertEquals(
            "Only",
            service.chooseCard("Pharmacies", 10000.0)
        )
    }

    @Test
    @Order(7)
    fun estimatesCashback() {
        service.addTransaction("Only", "Pharmacies", 10000.0)
        service.addTransaction("Alpha Credit", "Other", 1100.0)
        val estimate = service.estimateCashback()
        Assertions.assertEquals(
            setOf(
                "Tinkoff Credit" to 2000.0,
                "Alpha Credit" to 32.0,
                "Only" to 300.0
            ),
            estimate.toSet()
        )
    }
}