package com.sungjae.randomchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage

class ChatroomActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatroom)

        val mqttClient = connect()

        val btnSend = findViewById<AppCompatButton>(R.id.btnSend)
        val etMessage = findViewById<AppCompatEditText>(R.id.etMessage)

        btnSend.setOnClickListener {
            if (etMessage.text != null) {
                val message = etMessage.text.toString()
                etMessage.setText("")
                publish(mqttClient, makeTopic(), message)
            }
        }
    }

    private fun connect(): MqttClient {
        val url = "tcp://220.79.204.104:1883"
        val mqttClient = MqttClient(url, MqttClient.generateClientId(), null) //persistence 파라미터 안 주면 에러남;;
        mqttClient.connect()
        return mqttClient
    }

    private fun makeTopic(): String {
        return "test"
    }

    private fun publish(mqttClient: MqttClient, topic: String, message: String) {
        mqttClient.publish(topic, MqttMessage(message.toByteArray()))
    }
}