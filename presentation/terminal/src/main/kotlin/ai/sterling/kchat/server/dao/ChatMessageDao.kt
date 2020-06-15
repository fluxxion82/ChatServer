package ai.sterling.kchat.server.dao

import ai.sterling.kchat.domain.chat.model.ChatMessage
import ai.sterling.kchat.domain.chat.persistence.ChatDao
import java.util.*

/*
 * Don't have a database nor persisting messages on server,
 * but wanted to structure the code as if I did so it would
 * be easier to add
 */
class ChatMessageDao : ChatDao {
    private var messageList: MutableList<ChatMessage>
    private var chatMessage: ChatMessage? = null

    init {
        messageList = ArrayList()
    }

    override fun insertChatMessage(message: ChatMessage): Boolean {
        chatMessage = message
        return true
    }

    override fun insertAllMessages(messageList: List<ChatMessage>): Boolean {
        this.messageList = messageList.toMutableList()
        if (messageList.size == 1) { // convenience
            chatMessage = messageList[0]
        }

        return true
    }

    override fun getAllMessages(): List<ChatMessage> {
        return messageList
    }

    override fun getChatMessage(id: Int): ChatMessage? {
        return messageList.filter {
            it.id == id
        }[0]
    }

    override fun deleteAllMessages(): Boolean {
        messageList.clear()
        chatMessage = null

        return true
    }
}