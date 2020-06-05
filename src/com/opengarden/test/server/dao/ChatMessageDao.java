package com.opengarden.test.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.opengarden.test.server.model.ChatMessage;

/*
 * Don't have a database nor persisting messages on server,
 * but wanted to structure the code as if I did so it would
 * be easier to add
 */
public class ChatMessageDao implements ChatDao {
	private List<ChatMessage> mMessageList;
	private ChatMessage mChatMessage;

	public ChatMessageDao() {
		mMessageList = new ArrayList<>();
	}

	@Override
	public void storeChatMessage(ChatMessage message) {
		mChatMessage = message;
	}

	@Override
	public ChatMessage getChatMessage() {
		return mChatMessage;
	}

	@Override
	public void storeChatMessage(String chatJson) {
		Gson gson = new Gson();
		JsonElement jelem = gson.fromJson(chatJson, JsonElement.class);

		if (jelem.isJsonArray()) {
			JsonArray array = jelem.getAsJsonArray();
			if (array != null) {
				for (JsonElement je : array) {
					ChatMessage msg = new Gson().fromJson(je, ChatMessage.class);
					mMessageList.add(msg);
				}
			}
		} else {
			ChatMessage msg = gson.fromJson(jelem, ChatMessage.class);
			mMessageList.add(msg);
		}

		if (mMessageList.size() == 1) { // convenience
			mChatMessage = mMessageList.get(0);
		}
	}

	@Override
	public List<ChatMessage> getAllMessages() {
		return mMessageList;
	}

	@Override
	public void deleteAllMessages() {
		mMessageList.clear();
		mChatMessage = null;
	}
}
