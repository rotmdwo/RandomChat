package com.sungjae.randomchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sungjae.randomchat.request.MatchRequest
import com.sungjae.randomchat.response.ApiResponse
import com.sungjae.randomchat.response.MatchResponse
import kotlinx.coroutines.*
import org.eclipse.paho.client.mqttv3.MqttClient
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlin.coroutines.suspendCoroutine

class WaitingroomActivity : AppCompatActivity() {
    companion object {
        private lateinit var clientId: String
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waitingroom)

        clientId = MqttClient.generateClientId()

        // 코루틴 스코프를 사용하면 suspend 함수로 선언 안 해도 됨
        CoroutineScope(Dispatchers.IO).launch {
            getTopic()
        }
    }

    suspend fun getTopic() {
        val request = MatchRequest(clientId)

        val response = requestMatch(request)
        onMatchResponse(response)
    }

    private suspend fun requestMatch(request: MatchRequest) =
        withContext(Dispatchers.IO) {
            ApiRepository.instance.match(request)
        }

    private fun onMatchResponse(response: ApiResponse<MatchResponse>) {
        if(response.success && response.data != null) {
            // TODO: ChatroomActivity 실행
        } else {
            // TODO: 오류발생 처리
        }
    }
}