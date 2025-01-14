package com.example.sispaknlp

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.RESULTS_RECOGNITION
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sispaknlp.model.ChatMessage
import com.example.sispaknlp.model.ChatbotResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatbotActivity : AppCompatActivity() {
    private val chatList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var sendButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var voiceButton: Button
    private lateinit var speechRecognizer: SpeechRecognizer

    private val SPEECH_REQUEST_CODE = 100 // Define the request code for speech recognition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        sendButton = findViewById(R.id.sendButton)
        recyclerView = findViewById(R.id.recyclerView)
        messageInput = findViewById(R.id.messageInput)
        voiceButton = findViewById(R.id.voiceButton)

        chatAdapter = ChatAdapter(chatList)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SPEECH", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SPEECH", "Beginning of speech")
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d("SPEECH", "End of speech")
            }

            override fun onError(error: Int) {
                Log.e("SPEECH_ERROR", "Error: $error")
                Toast.makeText(applicationContext, "Error recognizing speech", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(RESULTS_RECOGNITION)
                val spokenText = data?.get(0) ?: ""
                Log.d("SPEECH_RESULT", "Spoken text: $spokenText")

                // Set the recognized text to the EditText field
                messageInput.setText(spokenText)
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        voiceButton.setOnClickListener {
            // Start speech recognition
            startSpeechRecognition()
        }

        sendButton.setOnClickListener {
            val userMessage = messageInput.text.toString().trim()
            Log.i("PESAN_MASUK", "Tombol Ditekan")
            if (userMessage.isNotEmpty()) {
                chatList.add(ChatMessage(userMessage, true))

                setButtonLoading(true)

                getChatbotResponse(userMessage) { response ->
                    chatList.add(ChatMessage(response, false))

                    chatAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(chatList.size - 1)
                    messageInput.text.clear()

                    setButtonLoading(false)
                }
            }
        }
    }

    private fun getChatbotResponse(message: String, callback: (String) -> Unit) {
        ChatbotClient.chatbotService.getMessage(message).enqueue(object : Callback<ChatbotResponse> {
            override fun onResponse(call: Call<ChatbotResponse>, response: Response<ChatbotResponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    res?.let {
                        Log.d("CHATBOT_RESPONSE", "Response: ${it.message}")
                        callback(it.message)
                    }
                } else {
                    Log.e("CHATBOT_ERROR", "Error: ${response.code()}")
                    callback("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ChatbotResponse>, t: Throwable) {
                Log.e("CHATBOT_FAILURE", "Failure: ${t.message}")
                callback("Failure: ${t.message}")
            }
        })
    }

    // Fungsi untuk mengubah status loading tombol kirim
    private fun setButtonLoading(isLoading: Boolean) {
        if (isLoading) {
            sendButton.text = "Loading..."
            sendButton.isEnabled = false
        } else {
            sendButton.text = "Kirim"
            sendButton.isEnabled = true
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID") // Optional: Use language code for Bahasa Indonesia
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    // Handle the result from the speech recognition activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            Log.d("SPEECH_RESULT", "Spoken text: $spokenText")

            // Set the recognized text to the EditText field
            messageInput.setText(spokenText)
        }
    }
}