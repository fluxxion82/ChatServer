package ai.sterling.kchat.server

import ai.sterling.kchat.domain.chat.model.ChatMessage

interface MessageListener {
    fun displayMessage(message: List<ChatMessage?>?)
    fun displayMessage(messageList: ChatMessage?)
}