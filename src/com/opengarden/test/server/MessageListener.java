package com.opengarden.test.server;

import java.util.List;

import com.opengarden.test.server.model.ChatMessage;


public interface MessageListener {
	public void displayMessage(List<ChatMessage> message);
	public void displayMessage(ChatMessage messageList);
}