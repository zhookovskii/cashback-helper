package app

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date

object Banks : IntIdTable() {
    val name = varchar("name", 50)
    val limit = double("limit").nullable()
    val pay = double("pay")
    val date = date("date")
}

class Bank(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Bank>(Banks)

    var name by Banks.name
    var limit by Banks.limit
    var pay by Banks.pay
    var date by Banks.date
}

object Cards : IntIdTable() {
    val name = varchar("name", 50)
    val bank = varchar("bank", 50)
    val cashback = double("cashback")
    val date = date("date")
}

class Card(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Card>(Cards)

    var name by Cards.name
    var bank by Cards.bank
    var cashback by Cards.cashback
    var date by Cards.date
}

object Cashbacks : IntIdTable() {
    val period = varchar("period", 10)
    val card = varchar("card", 50)
    val category = varchar("category", 50)
    val percent = double("percent")
    val permanent = bool("permanent")
    val date = date("date")
}

class Cashback(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Cashback>(Cashbacks)

    var period by Cashbacks.period
    var card by Cashbacks.card
    var category by Cashbacks.category
    var percent by Cashbacks.percent
    var permanent by Cashbacks.permanent
    var date by Cashbacks.date
}