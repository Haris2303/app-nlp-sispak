package com.example.sispaknlp

import com.example.sispaknlp.service.ChatbotService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ChatbotClient {

    private const val BASE_URL = "https://b4x2sbsk-5000.asse.devtunnels.ms/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val chatbotService: ChatbotService by lazy {
        retrofit.create(ChatbotService::class.java)
    }
}