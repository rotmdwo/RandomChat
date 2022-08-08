package com.sungjae.randomchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sungjae.randomchat.request.MatchRequest
import com.sungjae.randomchat.response.ApiResponse
import com.sungjae.randomchat.response.MatchResponse
import kotlinx.android.synthetic.main.activity_waitingroom.*
import kotlinx.coroutines.*
import org.eclipse.paho.client.mqttv3.MqttClient
import java.net.ConnectException
import java.util.*
import kotlin.concurrent.timerTask

class WaitingroomActivity : AppCompatActivity() {
    private lateinit var clientId: String
    private lateinit var loadingTimer: Timer
    private var isLoggingOut = false
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waitingroom)

        // TODO: Splash 화면으로 이동
        // SharedPreferences
        PreferenceManager.create(this)
        val serverIp = PreferenceManager.instance.getString("server_ip")
        if (serverIp == null || "" == serverIp) {
            database = Firebase.database.reference
            database.child("server_ip").get().addOnSuccessListener { dataSnapshot ->
                val serverIpAddress = dataSnapshot.getValue(String::class.java)
                serverIpAddress?.let {
                    ApiRepository.instance = ApiGenerator().generate(ApiRepository::class.java, hostUrl = it)
                    PreferenceManager.instance.putString("server_ip", value = it)
                } ?: run {
                    // serverIpAddress가 null이면 실행
                    Toast.makeText(this, getString(R.string.unable_to_connect_to_the_server), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            // SharedPreferences에 서버 저장되어 있음
            ApiRepository.instance = ApiGenerator().generate(ApiRepository::class.java, hostUrl = serverIp)
            // TODO: 서버에 ping 테스트, 실패하면 Firebase에서 서버 IP 새로 받기
        }


        clientId = MqttClient.generateClientId()

        btnBeginChat.setOnClickListener {
            setClickable(it, false)
            btnBeginChat.visibility = View.INVISIBLE
            initLoadingBar()
            isLoggingOut = false
            // 코루틴 스코프를 사용하면 suspend 함수로 선언 안 해도 됨
            CoroutineScope(Dispatchers.IO).launch {
                findMatch()
            }
        }
        btnCancelMatch.setOnClickListener {
            hideLoadingAndShowButton()
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
        hideLoadingAndShowButton()
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

    private fun hideLoadingAndShowButton() {
        llLoading.visibility = View.INVISIBLE
        btnBeginChat.visibility = View.VISIBLE
        setClickable(btnBeginChat, true)
    }

    /* Match API */
    private suspend fun findMatch() {
        val request = MatchRequest(clientId)

        runCatching {
            val response = requestMatch(request)
            return@runCatching response // onSuccess에 변수 전달
        }.onSuccess {
            onMatchResponse(it) // it == response
        }.onFailure {
            when (it) {
                is ConnectException -> runOnUiThread {
                    hideLoadingAndShowButton()
                    Toast.makeText(this, R.string.internet_connection_failed, Toast.LENGTH_SHORT).show()
                }
                else -> runOnUiThread {
                    hideLoadingAndShowButton()
                    Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Throws(ConnectException::class)
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
                        if (!isLoggingOut) findMatch()
                    }
                }, 1000)
            }
        } else {
            // 오류발생 재시도
            Timer().schedule(timerTask {
                CoroutineScope(Dispatchers.IO).launch {
                    if (!isLoggingOut) findMatch()
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