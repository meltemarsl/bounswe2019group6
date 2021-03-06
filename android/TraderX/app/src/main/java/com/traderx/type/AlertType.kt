package com.traderx.type

enum class AlertType(val request: String, val response: String) {
    ABOVE("above", "ABOVE"),
    BELOW("below", "BELOW")
}