package app

sealed class TransactionResult

object Success : TransactionResult()

data class Error(val message: String) : TransactionResult()