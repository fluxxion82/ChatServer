package com.opengarden.test.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.opengarden.test.server.dao.ChatDao;
import com.opengarden.test.server.dao.ChatMessageDao;
import com.opengarden.test.server.model.ChatMessage;

public class ClientThread extends Thread {
	private PrintWriter mOutWriter;
    private BufferedReader bufferedReader;
    private Socket mClientSocket;
    private List<MessageListener> mMessageListeners;
    private ChatDao mChatDao;
    
    private String mUsername;
    private boolean keepGoing = true;
 	public int id; // my unique id (easier for disconnecting)
 	
 	
    public ClientThread(Socket clientSocket, List<MessageListener> messageListeners) {
    	mMessageListeners = messageListeners;
        mClientSocket = clientSocket;
        mChatDao = new ChatMessageDao();
        
        id = ++ChatServer.uniqueId;
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
            mOutWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            bufferedReader = new BufferedReader(inputStreamReader);
            
            if (bufferedReader.ready()) {
            	String msg = bufferedReader.readLine(); // Read the chat message to get username
            	mChatDao.storeChatMessage(msg);
            	ChatMessage msgObj = mChatDao.getChatMessage();
            	
            	mUsername = msgObj.getUsername();
            	for(MessageListener listener : mMessageListeners) {
            		listener.displayMessage(msgObj);
            	}
                
            	// so i don't have to worry about finding the right messages later
            	mChatDao.deleteAllMessages(); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String message;
        
        while (keepGoing) {
            try {
                if (bufferedReader.ready()) {
                    message = bufferedReader.readLine(); // Read the chat message.
                    mChatDao.storeChatMessage(message);
                    List<ChatMessage> messageList = mChatDao.getAllMessages();

                    for(MessageListener listener : mMessageListeners) {
                		listener.displayMessage(messageList);
                	}
                    
                 // so i don't have to worry about finding the right messages later
                    mChatDao.deleteAllMessages();
                }
            } catch (IOException ex) {
                System.out.println("Problem in message reading");
                ex.printStackTrace();
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
            }
        }
    }


	public boolean writeMsg(String message) {
		// if Client is still connected send the message to it
		if (!mClientSocket.isConnected()) {
			kill();
			return false;
		}
		
		// Print the message on output stream.
		mOutWriter.println(message); 
		return true;
	}
    
	public void kill() {
		keepGoing = false;
	}

	// client username
	public String getUsername() {
		return mUsername;
	}

}