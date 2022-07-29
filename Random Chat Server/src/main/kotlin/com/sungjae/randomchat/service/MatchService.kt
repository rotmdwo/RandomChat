package com.sungjae.randomchat.service

import com.sungjae.randomchat.RandomChatApplication.Companion.hashMap
import com.sungjae.randomchat.RandomChatApplication.Companion.queue
import com.sungjae.randomchat.response.MatchResponse
import org.springframework.stereotype.Service
import com.sungjae.randomchat.request.MatchRequest
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

/* Service는 Request를 받아서 Response를 반환하는
 * 비즈니스 로직 처리하는 부분 */
@Service
class MatchService {

    fun match(matchRequest: MatchRequest): MatchResponse {
        val clientId = matchRequest.clientId

        if (hashMap.contains(clientId)) {
            // 이미 상대방 매칭된 경우
            println("Client ID [$clientId] has found a mate.")
            val topic = hashMap[clientId]
            hashMap.remove(clientId)
            return MatchResponse(topic)
        } else if (queue.isEmpty()) {
            // 매칭 기다리는 사람이 아무도 없는 경우
            println("Client ID [$clientId] is waiting to chat.")
            queue.add(matchRequest.clientId)
            return MatchResponse(null)
        } else {
            if (queue.peek() != clientId) {
                // 내가 들어와서 매칭된 경우
                println("Client ID [$clientId] has found a mate.")
                val theirId = queue.pollFirst()
                val topic = "${System.currentTimeMillis()}"
                hashMap[theirId] = topic
                return MatchResponse(topic)
            } else {
                // 아직도 나 혼자 기다리는 경우
                return MatchResponse(null)
            }
        }
    }
}