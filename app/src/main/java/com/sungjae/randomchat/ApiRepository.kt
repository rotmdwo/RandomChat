package com.sungjae.randomchat

import com.sungjae.randomchat.request.MatchRequest
import com.sungjae.randomchat.response.ApiResponse
import com.sungjae.randomchat.response.MatchResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiRepository {
    companion object {
        val instance = ApiGenerator().generate(ApiRepository::class.java)
    }

    @POST("/api/match")
    suspend fun match(@Body matchRequest: MatchRequest) : ApiResponse<MatchResponse>
}