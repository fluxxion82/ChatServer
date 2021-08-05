package ai.sterling.kchat.server

import ai.sterling.kchat.domain.chat.model.ChatMessage
import ai.sterling.logging.KLogger
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.datetime.Clock
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.channels.ClosedSendChannelException

class ChatServer {
    /**
     * Atomic counter used to get unique user-names based on the maxiumum users the server had.
     */
    private val usersCounter = AtomicInteger()

    /**
     * A concurrent map associating session IDs to user names.
     */
    private val memberNames = ConcurrentHashMap<String, String>()

    /**
     * A concurrent map associating session IDs to user names.
     */
    private val memberIds = ConcurrentHashMap<String, Int>()

    /**
     * Associates a session-id to a set of websockets.
     * Since a browser is able to open several tabs and windows with the same cookies and thus the same session.
     * There might be several opened sockets for the same client.
     */
    private val members = ConcurrentHashMap<String, MutableList<WebSocketSession>>()

    /**
     * A list of the lastest messages sent to the server, so new members can have a bit context of what
     * other people was talking about before joining.
     */
    private val lastMessages = LinkedList<ChatMessage>()

    /**
     * Handles that a member identified with a session id and a socket joined.
     */
    fun memberJoin(member: String, socket: WebSocketSession) {
        KLogger.i { "member join, $member" }
        println("member join, $member")
        // Checks if this user is already registered in the server and gives him/her a temporal name if required.
        memberNames.computeIfAbsent(member) { "user${usersCounter.incrementAndGet()}" }

        memberIds[member] = usersCounter.get()
        // Associates this socket to the member id.
        // Since iteration is likely to happen more frequently than adding new items,
        // we use a `CopyOnWriteArrayList`.
        // We could also control how many sockets we would allow per client here before appending it.
        // But since this is a sample we are not doing it.
        val list = members.computeIfAbsent(member) { CopyOnWriteArrayList() }
        list.add(socket)
    }

    suspend fun parseChatMessages(id: String, socket: WebSocketSession, text: String) {
        KLogger.d { "json msg: $text" }
        println("json msg: $text")
        try {
            val msgList = Gson().fromJson<List<ChatMessage>>(text, object : TypeToken<List<ChatMessage>?>() {}.type)
            msgList.forEach { msg ->
                when (msg.type) {
                    ChatMessage.LOGIN -> {
                        val name = if (msg.username.isNullOrEmpty()) memberNames[id]!! else msg.username
                        broadcastAll(msg.copy(username = name, message = "$name just joined"))

                    }
                    ChatMessage.LOGOUT -> {
                        memberLeft(id, socket)
                        broadcastAll(msg.copy(message = "${memberNames[id]} logged out"))
                    }
                    else -> {
                        memberNames[id] = msg.username
                        msg.copy(id = memberIds[id]!!)
                        checkCommands(id, msg.message)
                    }
                }
            }
        } catch (exception: JsonSyntaxException) {
            checkCommands(id, text)
        }
    }

    /**
     * We received a message. Let's process it.
     */
    suspend fun checkCommands(id: String, text: String) {
        KLogger.d { "receivedMessage, command: $text, id: $id" }
        println("receivedMessage, command: $text, id: $id")
        // We are going to handle commands (text starting with '/') and normal messages
        when {
            // The command `who` responds the user about all the member names connected to the user.
            text.startsWith("/who") -> who(id)
            // The command `user` allows the user to set its name.
            text.startsWith("/user") -> {
                // We strip the command part to get the rest of the parameters.
                // In this case the only parameter is the user's newName.
                val newName = text.removePrefix("/user").trim()
                // We verify that it is a valid name (in terms of length) to prevent abusing
                when {
                    newName.isEmpty() -> sendTo(id, "server::help", "/user [newName]")
                    newName.length > 50 -> sendTo(
                        id,
                        "server::help",
                        "new name is too long: 50 characters limit"
                    )
                    else -> memberRenamed(id, newName)
                }
            }
            // The command 'help' allows users to get a list of available commands.
            text.startsWith("/help") -> help(id)
            // If no commands matched at this point, we notify about it.
            text.startsWith("/") -> sendTo(
                id,
                "server::help",
                "Unknown command ${text.takeWhile { !it.isWhitespace() }}"
            )
            // Handle a normal message.
            else -> message(id, text)
        }
    }

    /**
     * Handles a [member] idenitified by its session id renaming [to] a specific name.
     */
    suspend fun memberRenamed(member: String, to: String) {
        // Re-sets the member name.
        val oldName = memberNames.put(member, to) ?: member
        // Notifies everyone in the server about this change.
        val msg = ChatMessage(
            memberIds[member]!!,
            to,
            ChatMessage.MESSAGE,
            "Member renamed from $oldName to $to",
            Clock.System.now().toEpochMilliseconds()
        )
        broadcast(msg)
    }

    /**
     * Handles that a [member] with a specific [socket] left the server.
     */
    suspend fun memberLeft(member: String, socket: WebSocketSession) {
        // Removes the socket connection for this member
        val connections = members[member]
        connections?.remove(socket)

        // If no more sockets are connected for this member, let's remove it from the server
        // and notify the rest of the users about this event.
        if (connections != null && connections.isEmpty()) {
            val name = memberNames.remove(member) ?: member
            val msg = ChatMessage(
                memberIds[member]!!,
                name,
                ChatMessage.LOGOUT,
                "${memberIds[member]!!} logged out",
                Clock.System.now().toEpochMilliseconds()
            )
            broadcast(msg)
        }
    }

    /**
     * Handles a [message] sent from a [sender] by notifying the rest of the users.
     */
    suspend fun message(sender: String, message: String) {
        // Pre-format the message to be send, to prevent doing it for all the users or connected sockets.
        val name = memberNames[sender]
        if (!name.isNullOrEmpty()) {
            KLogger.d { "name is not null" }

            // Sends this pre-formatted message to all the members in the server.
            val msg = ChatMessage(
                memberIds[sender]!!,
                name,
                ChatMessage.REPLY,
                message,
                Clock.System.now().toEpochMilliseconds()
            )
            broadcast(msg)

            // Appends the message to the list of [lastMessages] and caps that collection to 100 items to prevent
            // growing too much.
            synchronized(lastMessages) {
                lastMessages.add(msg)
                if (lastMessages.size > 100) {
                    lastMessages.removeFirst()
                }
            }
        } else {
            KLogger.d { "name IS null" }
        }
    }

    /**
     * Handles the 'who' command by sending the member a list of all all members names in the server.
     */
    suspend fun who(sender: String) {
        val message = ChatMessage(
            memberIds[sender]!!,
            memberNames[sender]!!,
            ChatMessage.MESSAGE,
            memberNames.values.joinToString(prefix = "[server::who] "),
            Clock.System.now().toEpochMilliseconds()
        )
        members[sender]?.send(Frame.Text(Gson().toJson(message).toString()))
    }

    /**
     * Handles the 'help' command by sending the member a list of available commands.
     */
    suspend fun help(sender: String) {
        val message = ChatMessage(
            memberIds[sender]!!,
            memberNames[sender]!!,
            ChatMessage.MESSAGE,
            "[server::help] Possible commands are: /user, /help and /who",
            Clock.System.now().toEpochMilliseconds()
        )
        members[sender]?.send(Frame.Text(Gson().toJson(message).toString()))
    }

    /**
     * Handles sending to a [recipient] from a [sender] a [message].
     *
     * Both [recipient] and [sender] are identified by its session-id.
     */
    suspend fun sendTo(recipient: String, sender: String, message: String) {
        val msg = ChatMessage(
            memberIds[sender]!!,
            memberNames[sender]!!,
            ChatMessage.MESSAGE,
            message,
            Clock.System.now().toEpochMilliseconds()
        )

        members[recipient]?.send(Frame.Text(Gson().toJson(msg).toString()))
    }

    /**
     * Sends a [message] to all the members in the server except the sender
     */
    private suspend fun broadcast(message: ChatMessage) {
        members.values.forEach { socket ->
            val ids = memberNames.filter {
                it.value == message.username
            }.keys
            if (ids.isNotEmpty() && members[ids.first()] != socket) {
                socket.send(Frame.Text(Gson().toJson(message).toString()))
            }
        }
    }

    /**
     * Sends a [message] to all the members in the server
     */
    private suspend fun broadcastAll(message: ChatMessage) {
        members.values.forEach { socket ->
            socket.send(Frame.Text(Gson().toJson(message).toString()))
        }
    }

    /**
     * Sends a [message] to a list of [this] [WebSocketSession].
     */
    suspend fun List<WebSocketSession>.send(frame: Frame) {
        forEach {
            try {
                it.send(frame.copy())
            } catch (t: Throwable) {
                try {
                    it.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                } catch (ignore: ClosedSendChannelException) {
                    // at some point it will get closed
                }
            }
        }
    }
}
