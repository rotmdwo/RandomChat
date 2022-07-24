package com.sungjae.randomchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

class ChatroomActivity : AppCompatActivity() {
    private lateinit var mqttClient: MqttClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatroom)

        mqttClient = connect()

        val btnSend = findViewById<AppCompatButton>(R.id.btnSend)
        val etMessage = findViewById<AppCompatEditText>(R.id.etMessage)
        val llChat = findViewById<LinearLayout>(R.id.llChat)
        val svChat = findViewById<ScrollView>(R.id.svChat)

        btnSend.setOnClickListener {
            if (etMessage.text.toString() != "") {
                val message = etMessage.text.toString()
                etMessage.setText("")
                publish(mqttClient, getTopic(), message) // 전송

                // 내 메시지 버블 삽입
                val view = LayoutInflater.from(this).inflate(R.layout.my_chat, null)
                val textView = view.findViewById<AppCompatTextView>(R.id.tvMessage)
                textView.text = message
                llChat.addView(view)

                svChat.fullScroll(ScrollView.FOCUS_DOWN) // 맨 밑으로 스크롤 다운
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mqttClient.isConnected) mqttClient.disconnect()
    }

    private fun connect(): MqttClient {
        val url = "tcp://220.79.204.104:1883"
        val mqttClient = MqttClient(url, MqttClient.generateClientId(), null) //persistence 파라미터 안 주면 에러남;;
        val connectOptions = MqttConnectOptions()
        connectOptions.userName = "test"
        connectOptions.password = "5534".toCharArray()
        mqttClient.connect(connectOptions)
        mqttClient.subscribe(getTopic())
        return mqttClient
    }

    private fun getTopic(): String {
        return "test"
    }

    private fun publish(mqttClient: MqttClient, topic: String, message: String) {
        mqttClient.publish(topic, MqttMessage(message.toByteArray()))
    }
}