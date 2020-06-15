package ai.sterling.kchat.server.view

import ai.sterling.kchat.domain.chat.model.ChatMessage
import ai.sterling.kchat.domain.chat.model.ChatMessage.Companion.REPLY
import ai.sterling.kchat.server.MessageListener
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea

class ChatWindow : JFrame(), MessageListener {
    private var mChatView: JTextArea? = null
    private var mSendButton: JButton? = null
    private var mClearButton: JButton? = null
    private var mChatBox: JTextArea? = null
    private var mCloseListener: OnChatCloseListener? = null
    private var mMessageListener: MessageListener? = null

    interface OnChatCloseListener {
        fun onChatClose()
    }

    fun open(closeListener: OnChatCloseListener?, messageListener: MessageListener?) {
        mCloseListener = closeListener
        mMessageListener = messageListener
        initComponents()
        setWinodwCloseListnerToCloseSocket()
        initSenderAndReceiver()
    }

    private fun initComponents() {
        mChatView = JTextArea(20, 46)
        val chatViewScrollPane = JScrollPane(mChatView)
        mChatBox = JTextArea(5, 40)
        val chatBoxScrollPane = JScrollPane(mChatBox)
        mSendButton = JButton("Send")
        mClearButton = JButton("Clear")
        isResizable = false
        title = "Chat Server"
        setSize(550, 500)
        val contentPane = contentPane
        contentPane.layout = FlowLayout()
        contentPane.add(chatViewScrollPane)
        contentPane.add(chatBoxScrollPane)
        contentPane.add(mSendButton)
        contentPane.add(mClearButton)
        mChatView!!.isEditable = false
        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true
    }

    private fun initSenderAndReceiver() {
        mSendButton!!.addActionListener { e: ActionEvent? ->
            val message = mChatBox!!.text
            val msg = ChatMessage(0, "Server", REPLY, message,
                    Date().time)
            mMessageListener!!.displayMessage(msg)
            displayMessage(msg)
            mChatBox!!.text = "" // Clear the chat box
        }
        mClearButton!!.addActionListener { e: ActionEvent? ->
            mChatView!!.text = "" // clear history
        }
    }

    private fun setWinodwCloseListnerToCloseSocket() {
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                mCloseListener!!.onChatClose()
            }
        })
    }

    override fun displayMessage(message: ChatMessage?) {
        mChatView!!.append(message.toString())
    }

    override fun displayMessage(messageList: List<ChatMessage?>?) {
        for (message in messageList!!) {
            displayMessage(message)
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
