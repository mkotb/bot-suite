package com.mazenk.telegram

import com.jtelegram.api.TelegramBotRegistry
import com.jtelegram.api.update.PollingUpdateProvider
import com.mazenk.telegram.aesthethicc.AesthethiccBot
import com.mazenk.telegram.dicktionary.DicktionaryBot
import com.mazenk.telegram.googram.GoogramBot
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.net.InetSocketAddress
import kotlin.system.exitProcess

fun main() {
    val registry = TelegramBotRegistry.builder()
            .updateProvider(PollingUpdateProvider())
            .build()

    AesthethiccBot(registry)
    GoogramBot(registry)
    DicktionaryBot(registry)
    setupHealthServer()

    println("Start up complete!")
}

fun setupHealthServer() {
    val healthServer: HttpServer

    try {
         healthServer = HttpServer.create(InetSocketAddress(80), 0)
    } catch (e: IOException) {
        println("Unable to start health server! Shutting down...")
        e.printStackTrace()
        exitProcess(-127)
    }

    healthServer.createContext("/").handler = HttpHandler {
        val response = "OK".toByteArray()
        val output = it.responseBody

        it.sendResponseHeaders(200, response.size.toLong())
        output.write(response)
        output.flush()
        it.close()
    }

    healthServer.start()
}
