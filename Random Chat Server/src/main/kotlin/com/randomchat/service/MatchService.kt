package com.randomchat.service

import com.randomchat.response.MatchResponse
import org.springframework.stereotype.Service
import com.randomchat.request.MatchRequest
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

/* Service는 Request를 받아서 Response를 반환하는
 * 비즈니스 로직 처리하는 부분 */
@Service
class MatchService {
    companion object {
        val queue = LinkedList<String>()
        val hashMap = HashMap<String, String>()
    }
    fun match(matchRequest: MatchRequest): MatchResponse {
        val clientId = matchRequest.clientId

        if (hashMap.contains(clientId)) {
            // 이미 상대방 매칭된 경우
            val topic = hashMap[clientId]
            hashMap.remove(clientId)
            return MatchResponse(topic)
        } else if (queue.isEmpty()) {
            // 매칭 기다리는 사람이 아무도 없는 경우
            queue.add(matchRequest.clientId)
            return MatchResponse(null)
        } else {
            // 내가 들어와서 매칭된 경우
            val theirId = queue.pollFirst()
            val topic = "${System.currentTimeMillis()}"
            hashMap[theirId] = topic
            return MatchResponse(topic)
        }
    }
}