package com.sungjae.randomchat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*
import kotlin.collections.HashMap

@SpringBootApplication
class RandomChatApplication {
    companion object {
        val queue = LinkedList<String>()
        val hashMap = HashMap<String, String>()
    }
}

fun main() {
    runApplication<RandomChatApplication>()
}