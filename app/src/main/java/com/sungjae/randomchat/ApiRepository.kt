package com.sungjae.randomchat

import com.sungjae.randomchat.request.MatchRequest
import com.sungjae.randomchat.response.ApiResponse
import com.sungjae.randomchat.response.MatchResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiRepository {
    companion object {
        lateinit var instance: ApiRepository
    }

    @POST("/api/match")
    suspend fun match(@Body matchRequest: MatchRequest) : ApiResponse<MatchResponse>

    @GET("/api/logout")
    suspend fun logout(@Query("request") clientId: String) : ApiResponse<String>
}