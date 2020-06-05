package ai.sterling.test.server;

import java.util.List;

import ai.sterling.test.server.model.ChatMessage;

public interface MessageListener {
	public void displayMessage(List<ChatMessage> message);

	public void displayMessage(ChatMessage messageList);
}