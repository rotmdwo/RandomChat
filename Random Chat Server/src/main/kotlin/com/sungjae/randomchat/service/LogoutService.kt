package com.sungjae.randomchat.service

import com.sungjae.randomchat.RandomChatApplication.Companion.hashMap
import com.sungjae.randomchat.RandomChatApplication.Companion.queue
import org.springframework.stereotype.Service

@Service
class LogoutService {
    fun logout(clientId: String): String {

        try {
            queue.remove(clientId)
            hashMap.remove(clientId)
        } catch (e: Exception) {

        }

        println("Client ID [$clientId] logged out.")
        return "ok"
    }
}