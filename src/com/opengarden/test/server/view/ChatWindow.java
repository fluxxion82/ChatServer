package com.opengarden.test.server.view;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.opengarden.test.server.MessageListener;
import com.opengarden.test.server.model.ChatMessage;

public class ChatWindow extends JFrame implements MessageListener {
	private static final long serialVersionUID = 1L;
	private JTextArea mChatView;
	private JButton mSendButton;
	private JButton mClearButton;
	private JTextArea mChatBox;
	private OnChatCloseListener mCloseListener;
	private MessageListener mMessageListener;

	public interface OnChatCloseListener {
		public void onChatClose();
	}

	public void open(OnChatCloseListener closeListener, MessageListener messageListener) {
		mCloseListener = closeListener;
		mMessageListener = messageListener;

		initComponents();

		setWinodwCloseListnerToCloseSocket();

		initSenderAndReceiver();
	}

	private void initComponents() {
		mChatView = new JTextArea(20, 46);
		JScrollPane chatViewScrollPane = new JScrollPane(mChatView);
		mChatBox = new JTextArea(5, 40);
		JScrollPane chatBoxScrollPane = new JScrollPane(mChatBox);
		mSendButton = new JButton("Send");
		mClearButton = new JButton("Clear");

		setResizable(false);
		setTitle("Chat Server");
		setSize(550, 500);
		Container contentPane = getContentPane();
		contentPane.setLayout(new FlowLayout());
		contentPane.add(chatViewScrollPane);
		contentPane.add(chatBoxScrollPane);
		contentPane.add(mSendButton);
		contentPane.add(mClearButton);
		mChatView.setEditable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void initSenderAndReceiver() {
		mSendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = mChatBox.getText();

				ChatMessage msg = new ChatMessage(0, "Server", ChatMessage.MESSAGE, message, new Date().getTime());
				mMessageListener.displayMessage(msg);
				displayMessage(msg);

				mChatBox.setText(""); // Clear the chat box
			}
		});

		mClearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mChatView.setText(""); // clear history
			}
		});
	}

	private void setWinodwCloseListnerToCloseSocket() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				mCloseListener.onChatClose();
			}
		});
	}

	@Override
	public void displayMessage(ChatMessage message) {
		mChatView.append(message.toString());
	}

	@Override
	public void displayMessage(List<ChatMessage> messageList) {
		for (ChatMessage message : messageList) {
			displayMessage(message);
		}
	}

}
