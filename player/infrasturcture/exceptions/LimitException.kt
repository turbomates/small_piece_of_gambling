package io.betforge.player.infrasturcture.exceptions

import io.betforge.infrastructure.domain.Currency

class LimitException(message: String) : Exception(message)
class LimitAlreadyExists(currency: Currency) : Exception("$currency limit already exists")
class InvalidFinancialLimitType(message: String = "Invalid financial limit type") : Exception(message)
