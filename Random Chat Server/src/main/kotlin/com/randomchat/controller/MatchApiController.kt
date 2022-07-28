package com.randomchat.controller

import com.randomchat.common.ApiResponse
import com.randomchat.request.MatchRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import com.randomchat.service.MatchService

@RestController
@RequestMapping("/api")
class MatchApiController @Autowired constructor(private val matchService: MatchService)
{
    @PostMapping("/match")
    fun match(@RequestBody matchRequest: MatchRequest) = ApiResponse.ok(matchService.match(matchRequest))
}