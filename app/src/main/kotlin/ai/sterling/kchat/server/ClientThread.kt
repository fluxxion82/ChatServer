package ai.sterling.kchat.server

import ai.sterling.kchat.domain.chat.model.ChatMessage
import ai.sterling.kchat.domain.chat.model.ChatMessage.Companion.LOGOUT
import ai.sterling.kchat.domain.chat.model.ChatMessage.Companion.MESSAGE
import ai.sterling.kchat.domain.chat.persistence.ChatDao
import ai.sterling.kchat.server.dao.ChatMessageDao
import ai.sterling.logger.KLogger
import com.google.gson.Gson
import com.google.gson.JsonElement
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ClientThread(private val clientSocket: Socket, private val messageListeners: List<MessageListener>) : Thread() {
    private lateinit var outWriter: PrintWriter
    private lateinit var bufferedReader: BufferedReader
    private val chatDao: ChatDao

    // client username
    var username: String? = null
    private var keepGoing = true
    // my unique id (easier for disconnecting)
	var id : Int

    init {
        chatDao = ChatMessageDao()
        id = ++KChatServer.uniqueId
        try {
            val inputStreamReader = InputStreamReader(clientSocket.getInputStream())
            outWriter = PrintWriter(clientSocket.getOutputStream(), true)
            bufferedReader = BufferedReader(inputStreamReader)
            if (bufferedReader.ready()) {
                val msg = bufferedReader.readLine() // Read the chat message
                val messages = parseChatMessage(msg)
                println("init num of messages ${messages.size}")
                if (messages.isNotEmpty()) {
                    chatDao.insertAllMessages(messages)
                    username = messages[0].username
                    println("init username: $username")
                    KLogger.d { "init username: $username" }
                    for (listener in messageListeners) {
                        listener.displayMessage(messages[0])
                    }

                    // so i don't have to worry about finding the right messages
                    // later
                    chatDao.deleteAllMessages()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun parseChatMessage(chatJson: String): List<ChatMessage> {
        val messageList = mutableListOf<ChatMessage>()
        val gson = Gson()
        val jelem = gson.fromJson(chatJson, JsonElement::class.java)
        if (jelem.isJsonArray) {
            val array = jelem.asJsonArray
            if (array != null) {
                for (je in array) {
                    val msg = Gson().fromJson(je, ChatMessage::class.java)
                    messageList.add(msg)
                }
            }
        } else {
            val msg = gson.fromJson(jelem, ChatMessage::class.java)
            messageList.add(msg)
        }

        return messageList
    }

    override fun run() {
        var message: String?
        while (keepGoing) {
            try {
                if (bufferedReader.ready()) {
                    message = bufferedReader.readLine() // Read the chat
                    val parsed = parseChatMessage(message)
                    parsed.forEach {
                        println("parsed message: ${it.message}")
                        KLogger.d {
                            "parsed message: ${it.message}"
                        }
                    }

                    username = parsed[0].username
                    println("username: $username")

                    chatDao.insertAllMessages(parsed)
                    val messageList = chatDao.getAllMessages()
                    for (listener in messageListeners) {
                        listener.displayMessage(messageList)
                    }

                    // so i don't have to worry about finding the right messages
                    // later
                    chatDao.deleteAllMessages()
                }
            } catch (ex: IOException) {
                println("Problem in message reading")
                KLogger.e(ex) {
                    "Problem in message reading"
                }
                ex.printStackTrace()
            }
            try {
                sleep(500)
            } catch (ie: InterruptedException) {
                println("Client thread interrupted")
                KLogger.e(ie) {
                    "Client thread interrupted"
                }
            }
        }
    }

    fun writeMsg(message: ChatMessage): Boolean {
        println("write msg, type=${message.type}, username=${username}, message=${message.message}")
        KLogger.d {
            "write msg, type=${message.type}, username=${username}, message=${message.message}"
        }

        // if Client is still connected send the message to it
        if (!clientSocket.isConnected || message.type == LOGOUT || (message.type == MESSAGE && username.isNullOrEmpty())) {
            println("not connected or username is null , username=${username}, message=${message.message}")
            KLogger.d {
                "not connected or username is null, username=${username}, message=${message.message}"
            }
            kill()
            return false
        }

        if (message.username != username) {
            val msg = Gson().toJson(message)
            // Print the message on output stream.
            outWriter.println(msg)
        }

        return true
    }

    fun kill() {
        keepGoing = false
    }
}