package com.example.sispaknlp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.RESULTS_RECOGNITION
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sispaknlp.model.ChatMessage
import com.example.sispaknlp.model.ChatbotResponse
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class ChatbotActivity : AppCompatActivity(), OnInitListener, NavigationView.OnNavigationItemSelectedListener {
    private val chatList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var sendButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var voiceButton: ImageButton
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var clearButton: MenuItem
    private lateinit var drawer_layout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var nav_view: NavigationView

    private val SPEECH_REQUEST_CODE = 100 // Define the request code for speech recognition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        // Initialize SharedPreferences for storing chat data
        sharedPreferences = getSharedPreferences("ChatPrefs", MODE_PRIVATE)

        sendButton = findViewById(R.id.sendButton)
        recyclerView = findViewById(R.id.recyclerView)
        messageInput = findViewById(R.id.messageInput)
        voiceButton = findViewById(R.id.voiceButton)
        drawer_layout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        nav_view = findViewById(R.id.nav_view)

        // Set Navigation Listener
        nav_view.setNavigationItemSelectedListener(this)

        // Setup Toolbar
        setSupportActionBar(toolbar)

        // Setup Drawer
        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        textToSpeech = TextToSpeech(this, this)

        chatAdapter = ChatAdapter(this, chatList)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        // Load previous chat messages
        loadChatMessages()

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

                // Save the user message to SharedPreferences
                saveChatMessages()

                chatAdapter.notifyDataSetChanged()

                getChatbotResponse(userMessage) { response ->
                    chatList.add(ChatMessage(response, false))

                    recyclerView.scrollToPosition(chatList.size - 1)
                    messageInput.text.clear()

//                    btnTTS.setOnClickListener {
//                        // Mengubah respons chatbot menjadi suara
//                        speakText(response)
//                    }

                    // Save the chatbot response to SharedPreferences
                    saveChatMessages()

                    setButtonLoading(false)
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clearButton -> {
                Toast.makeText(this, "Chat clear", Toast.LENGTH_SHORT).show()
                chatAdapter.clearMessages() // Menghapus seluruh pesan
                sharedPreferences.edit().clear().apply() // Menghapus data chat di SharedPreferences
                true
            }
            else -> super.onOptionsItemSelected(item)
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
                callback("Mohon maaf server lagi bad mood")
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
            val textInput = messageInput.text
            messageInput.setText(textInput.append(" $spokenText"))
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val langResult = textToSpeech.setLanguage(Locale("id", "ID"))
            Log.i("Language_Result", "Result: $langResult")

            // Menangani berbagai kemungkinan hasil
            when (langResult) {
                TextToSpeech.LANG_AVAILABLE -> {
                    Log.d("TTS", "Bahasa Indonesia tersedia dan siap digunakan.")
                }
                TextToSpeech.LANG_MISSING_DATA -> {
                    Log.e("TTS", "Data bahasa Indonesia hilang.")
                    Toast.makeText(this, "Data bahasa Indonesia hilang, silakan unduh data TTS.", Toast.LENGTH_LONG).show()
                }
                TextToSpeech.LANG_NOT_SUPPORTED -> {
                    Log.e("TTS", "Bahasa Indonesia tidak didukung.")
                    Toast.makeText(this, "Bahasa Indonesia tidak didukung.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Log.e("TTS", "Status bahasa tidak diketahui atau error.")
                }
            }
        } else {
            Log.e("TTS", "Inisialisasi TTS gagal!")
            Toast.makeText(this, "Inisialisasi TTS gagal.", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk mengubah respons chatbot menjadi suara
    private fun speakText(text: String) {
        Log.i("IS_LANGUANGE_TTS", "Result: ${textToSpeech.isLanguageAvailable(Locale("id", "ID"))} ----- Lang Available: ${TextToSpeech.LANG_AVAILABLE}")
        if (textToSpeech.isLanguageAvailable(Locale("id", "ID")) == 1) {
            textToSpeech.language = Locale("id", "ID") // Bahasa Indonesia
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("TTS", "Language not supported!")
        }
    }

    // Save the chat messages to SharedPreferences
    private fun saveChatMessages() {
        val editor = sharedPreferences.edit()
        val chatJson = Gson().toJson(chatList) // Convert chat list to JSON
        editor.putString("chatMessages", chatJson)
        editor.apply()
    }

    // Load the chat messages from SharedPreferences
    private fun loadChatMessages() {
        val chatJson = sharedPreferences.getString("chatMessages", "[]")
        val chatType = object : TypeToken<List<ChatMessage>>() {}.type
        val chatListFromPrefs: List<ChatMessage> = Gson().fromJson(chatJson, chatType)

        chatList.clear()
        chatList.addAll(chatListFromPrefs)

        chatAdapter.notifyDataSetChanged()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Pindah ke chatbot activity
                startActivity(Intent(this, ChatbotActivity::class.java))
            }
            R.id.nav_info -> {
                // pindah ke information activity
                startActivity(Intent(this, InformationActivity::class.java))
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
