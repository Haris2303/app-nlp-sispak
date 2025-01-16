package com.example.sispaknlp

import android.content.Context
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.sispaknlp.model.ChatMessage
import org.w3c.dom.Text
import java.util.Locale

class ChatAdapter(private val context: Context, private val chatList: MutableList<ChatMessage>): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>(), OnInitListener {

    private var textToSpeech: TextToSpeech = TextToSpeech(context, this)

    inner class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userMessage: TextView = itemView.findViewById(R.id.userMessage)
        val botMessage: TextView = itemView.findViewById(R.id.botMessage)
        val ibTextToSpeech: ImageButton = itemView.findViewById(R.id.ibTextToSpeech)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_bubble, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = chatList[position]
        if (chatMessage.isUser) {
            holder.userMessage.text = chatMessage.message
            holder.userMessage.visibility = View.VISIBLE
            holder.botMessage.visibility = View.GONE
            holder.ibTextToSpeech.visibility = View.GONE
        } else {
            holder.botMessage.text = chatMessage.message
            holder.botMessage.visibility = View.VISIBLE
            holder.userMessage.visibility = View.GONE
            holder.ibTextToSpeech.setOnClickListener{
                speakText(chatMessage.message)
            }
        }
    }

    // Fungsi untuk menghapus seluruh pesan
    fun clearMessages() {
        chatList.clear() // Menghapus semua pesan dalam chatList
        notifyDataSetChanged() // Memberi tahu adapter untuk memperbarui RecyclerView
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
                    Toast.makeText(context, "Data bahasa Indonesia hilang, silakan unduh data TTS.", Toast.LENGTH_LONG).show()
                }
                TextToSpeech.LANG_NOT_SUPPORTED -> {
                    Log.e("TTS", "Bahasa Indonesia tidak didukung.")
                    Toast.makeText(context, "Bahasa Indonesia tidak didukung.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Log.e("TTS", "Status bahasa tidak diketahui atau error.")
                }
            }
        } else {
            Log.e("TTS", "Inisialisasi TTS gagal!")
            Toast.makeText(context, "Inisialisasi TTS gagal.", Toast.LENGTH_SHORT).show()
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
}