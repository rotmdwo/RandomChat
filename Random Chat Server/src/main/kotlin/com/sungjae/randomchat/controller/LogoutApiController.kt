package com.sungjae.randomchat.controller

import com.sungjae.randomchat.common.ApiResponse
import com.sungjae.randomchat.service.LogoutService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class LogoutApiController @Autowired constructor(private val logoutService: LogoutService)
{
    @GetMapping("/logout")
    fun logout(@RequestParam(name = "request") clientId: String) = ApiResponse.ok(logoutService.logout(clientId))
}