package com.sungjae.randomchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.android.synthetic.main.activity_chatroom.*
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.timerTask

class ChatroomActivity : AppCompatActivity() {
    private val endOfChatCode = "272c68863af4c821491753d9ae636b86f74e1a11" // End of Chat : SHA1
    private var notifiedEndOfChat = false
    private lateinit var mqttClient: MqttClient
    private lateinit var clientId: String
    private lateinit var topic: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatroom)

        // TODO: 에러처리 필요. null 시 종료
        clientId = intent.getStringExtra("clientId")!!
        topic = intent.getStringExtra("topic")!!

        connect()

        btnSend.setOnClickListener {
            if (etMessage.text.toString() != "") {
                val message = etMessage.text.toString()
                etMessage.setText("")

                // 내 메시지 버블 삽입
                val view = LayoutInflater.from(this).inflate(R.layout.my_chat, null)
                val textView = view.findViewById<AppCompatTextView>(R.id.tvMyMessage)
                textView.text = message
                llChat.addView(view)

                Timer().schedule(timerTask { svChat.smoothScrollTo(0, llChat.height) }, 300) // 시간차 안 주면 새로운 view 넣기 전 height으로 인식 함
                //svChat.fullScroll(ScrollView.FOCUS_DOWN) // 맨 밑으로 스크롤 다운

                publish(mqttClient, getTopic(), message) // 전송
            }
        }

        btnExit.setOnClickListener {
            notifyEndOfChat()
            notifiedEndOfChat = true
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!notifiedEndOfChat) notifyEndOfChat()
        if (mqttClient.isConnected) mqttClient.disconnect()
    }

    private fun connect() {
        val url = "tcp://220.79.204.104:1883"

        mqttClient = MqttClient(url, clientId, null) //persistence 파라미터 안 주면 에러남;;
        val connectOptions = MqttConnectOptions()
        connectOptions.userName = "test"
        connectOptions.password = "5534".toCharArray()
        mqttClient.connect(connectOptions)

        // 수신 설정
        mqttClient.subscribe(getTopic())
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.e(application.packageName, "Connection Lost")
                connect()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                if (topic == getTopic() && message != null) {
                    val jsonObject = JSONObject(String(message.payload))
                    val theirId = jsonObject.getString("id")
                    val theirMessage = jsonObject.getString("content")

                    if (theirId != clientId) {
                        if (endOfChatCode == theirMessage) {
                            // 상대방이 방에서 나감

                            etMessage.isEnabled = false
                            btnSend.isClickable = false

                            runOnUiThread {
                                val leftMessageView = LayoutInflater.from(this@ChatroomActivity).inflate(R.layout.left_message, null)
                                llChat.addView(leftMessageView)
                            }
                        } else {
                            // 콜백함수 안에서 runOnUiThread 안 쓰면 UI 업데이트 안 됨
                            runOnUiThread {
                                // 상대 메시지 버블 삽입
                                val view = LayoutInflater.from(this@ChatroomActivity).inflate(R.layout.their_chat, null)
                                val textView = view.findViewById<AppCompatTextView>(R.id.tvTheirMessage)
                                textView.text = theirMessage
                                llChat.addView(view)
                            }
                        }
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun getTopic(): String {
        return topic
    }

    private fun publish(mqttClient: MqttClient, topic: String, message: String) {
        val json = "{\"id\":\""+clientId+"\",\"content\":\""+message+"\"}"
        mqttClient.publish(topic, MqttMessage(json.toByteArray()))
    }

    private fun notifyEndOfChat() {
        publish(mqttClient, getTopic(), endOfChatCode)
    }
}