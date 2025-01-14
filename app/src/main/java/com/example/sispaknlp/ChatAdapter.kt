package com.example.sispaknlp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sispaknlp.model.ChatMessage

class ChatAdapter(private val chatList: List<ChatMessage>): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userMessage: TextView = itemView.findViewById(R.id.userMessage)
        val botMessage: TextView = itemView.findViewById(R.id.botMessage)
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
        } else {
            holder.botMessage.text = chatMessage.message
            holder.botMessage.visibility = View.VISIBLE
            holder.userMessage.visibility = View.GONE
        }
    }
}