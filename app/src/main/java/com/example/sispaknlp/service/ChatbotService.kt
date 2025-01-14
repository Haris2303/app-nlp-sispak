package com.example.sispaknlp.service

import com.example.sispaknlp.model.ChatbotResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ChatbotService {
    @GET("api")
    fun getMessage(@Query("quetion") quetion: String): Call<ChatbotResponse>
}