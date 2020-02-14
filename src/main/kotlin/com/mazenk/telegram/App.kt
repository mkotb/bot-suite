package com.mazenk.telegram

import com.jtelegram.api.TelegramBotRegistry
import com.jtelegram.api.update.PollingUpdateProvider
import com.mazenk.telegram.aesthethicc.AesthethiccBot

fun main() {
    val registry = TelegramBotRegistry.builder()
            .updateProvider(PollingUpdateProvider())
            .build()

    AesthethiccBot(registry)
    //
}
