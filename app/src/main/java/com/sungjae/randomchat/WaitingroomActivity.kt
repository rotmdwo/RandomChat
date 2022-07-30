package com.sungjae.randomchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.ContentLoadingProgressBar
import com.sungjae.randomchat.request.MatchRequest
import com.sungjae.randomchat.response.ApiResponse
import com.sungjae.randomchat.response.MatchResponse
import kotlinx.coroutines.*
import org.eclipse.paho.client.mqttv3.MqttClient
import java.util.*
import kotlin.concurrent.timerTask

class WaitingroomActivity : AppCompatActivity() {
    private lateinit var clientId: String
    private lateinit var btnBeginChat: AppCompatButton
    private lateinit var pbLoading: ContentLoadingProgressBar
    private lateinit var llLoading: LinearLayout
    private lateinit var btnCancelMatch: AppCompatButton
    private lateinit var loadingTimer: Timer
    var isLoggingOut = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waitingroom)

        clientId = MqttClient.generateClientId()

        btnBeginChat = findViewById(R.id.btnBeginChat)
        pbLoading = findViewById(R.id.pbLoading)
        llLoading = findViewById(R.id.llLoading)
        btnCancelMatch = findViewById(R.id.btnCancelMatch)

        btnBeginChat.setOnClickListener {
            setClickable(it, false)
            btnBeginChat.visibility = View.INVISIBLE
            initLoadingBar()
            isLoggingOut = false
            // 코루틴 스코프를 사용하면 suspend 함수로 선언 안 해도 됨
            CoroutineScope(Dispatchers.IO).launch {
                getTopic()
            }
        }
        btnCancelMatch.setOnClickListener {
            llLoading.visibility = View.INVISIBLE
            btnBeginChat.visibility = View.VISIBLE
            setClickable(btnBeginChat, true)
            isLoggingOut = true
            loadingTimer.cancel()
            CoroutineScope(Dispatchers.IO).launch {
                logout()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setClickable(btnBeginChat, true)
        llLoading.visibility = View.INVISIBLE
        btnBeginChat.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        llLoading.visibility = View.INVISIBLE
        btnBeginChat.visibility = View.VISIBLE
        setClickable(btnBeginChat, true)
        isLoggingOut = true
        loadingTimer.cancel()
        CoroutineScope(Dispatchers.IO).launch {
            logout()
        }
    }

    private fun setClickable(view: View, isTrue: Boolean) {
        view.isClickable = isTrue
    }

    private fun initLoadingBar() {
        llLoading.visibility = View.VISIBLE
        pbLoading.progress = 0
        loadingTimer = Timer()
        loadingTimer.scheduleAtFixedRate(timerTask {
            if (pbLoading.progress == 100) {
                pbLoading.progress = 0
            } else {
                pbLoading.progress += 1
            }
        }, 0L, 50L)
    }

    /* Match API */
    private suspend fun getTopic() {
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
            if (response.data.topic != null) {
                // 매칭 됨.
                val intent = Intent(this, ChatroomActivity::class.java)
                intent.putExtra("clientId", clientId)
                intent.putExtra("topic", response.data.topic)
                startActivity(intent)
            } else {
                // 기다리는 사람 없음. 다시 호출 필요
                Timer().schedule(timerTask {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (!isLoggingOut) getTopic()
                    }
                }, 1000)
            }
        } else {
            // 오류발생 재시도
            Timer().schedule(timerTask {
                CoroutineScope(Dispatchers.IO).launch {
                    if (!isLoggingOut) getTopic()
                }
            }, 1000)
        }
    }

    /* Logout API */
    private suspend fun logout() {
        val response = requestLogout(clientId)
        onLogoutResponse(response)
    }

    private suspend fun requestLogout(clientId: String) =
        withContext(Dispatchers.IO) {
            ApiRepository.instance.logout(clientId)
        }

    private fun onLogoutResponse(response: ApiResponse<String>) {
        if (response.data == "ok") Log.d("API", "logged out successfully.")
        else Log.d("API", "log out error.")
    }
}