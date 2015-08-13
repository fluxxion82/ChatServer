package com.opengarden.test.server.dao;

import java.util.List;

import com.opengarden.test.server.model.ChatMessage;

/*
 * Don't have a database nor persisting messages on server,
 * but wanted to structure the code as if I did so it would
 * be easier to add
 */
public interface ChatDao {
	public void storeChatMessage(ChatMessage message);
	public void storeChatMessage(String chatJson);
	public ChatMessage getChatMessage();
	public List<ChatMessage> getAllMessages();
	public void deleteAllMessages();
}
