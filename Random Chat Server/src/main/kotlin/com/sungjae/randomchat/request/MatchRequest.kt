package com.sungjae.randomchat.request

/* data class 형식으로 하니까
 * Resolved [org.springframework.http.converter.HttpMessageNotReadableException: JSON parse error: Cannot construct instance of `com.randomchat.request.MatchRequest`
 * 에러 발생해서 기본 constructor 가진 형태의 클래스로 변경함 */
class MatchRequest {
    lateinit var clientId: String

    constructor(clientId: String) {
        this.clientId = clientId
    }

    constructor()
}