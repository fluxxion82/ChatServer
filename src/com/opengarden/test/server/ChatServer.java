package com.opengarden.test.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.opengarden.test.server.model.ChatMessage;
import com.opengarden.test.server.view.ChatWindow;
import com.opengarden.test.server.view.ChatWindow.OnChatCloseListener;

public class ChatServer implements OnChatCloseListener, MessageListener {
	private ServerSocket mServerSocket = null;

	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> mClients;

	// a unique ID for each connection
	public static int uniqueId;

	// flag for stopping server
	private boolean keepGoing = true;

	// the port number to listen for connection
	private int mPort;

	public ChatServer(int port) {
		this.mPort = port;

		// Client list
		mClients = new ArrayList<ClientThread>();
	}

	/*
	 * Main
	 */
	public static void main(String[] args) {
		// start server on port 4444 unless a PortNumber is specified
		int portNumber = 4444;
		switch (args.length) {
		case 1:
			try {
				portNumber = Integer.parseInt(args[0]);
				// portNumber = Integer.valueOf(System.getenv("PORT"));
			} catch (Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Usage is: > java Server [portNumber]");
				return;
			}
		case 0:
			break;
		default:
			System.out.println("Usage is: > java Server [portNumber]");
			return;

		}

		try {
			System.out.println(Integer.valueOf(System.getenv("PORT")));

		} catch (Exception e) {

		}

		ChatServer server = new ChatServer(portNumber);
		server.start();
	}

	public void start() {
		Socket clientSocket = null;

		try {
			mServerSocket = new ServerSocket(mPort);

			ChatWindow chatWindow = new ChatWindow();
			chatWindow.open(ChatServer.this, this);

			System.out.println("Server started. Listening to the port " + mPort + ". Waitng for a client to connect.");

			// infinite loop to wait for connections
			while (keepGoing) {
				clientSocket = mServerSocket.accept();

				System.out.println("Client connected on port " + mPort);

				// if was asked to stop
				if (!keepGoing) {
					break;
				}

				ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
				listeners.add(this);
				listeners.add(chatWindow);
				ClientThread receiver = new ClientThread(clientSocket, listeners);

				mClients.add(receiver);
				receiver.start();
			}

			mServerSocket.close();

		} catch (IOException e) {
			System.out.println("Could not listen on port: " + mPort);
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void displayMessage(ChatMessage message) {
		String messageLf = "";

		// loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for (int i = mClients.size(); --i >= 0;) {
			ClientThread ct = mClients.get(i);

			messageLf = new Gson().toJson(message);

			// try to write to the Android Client if it fails remove it from the
			// list
			if (!ct.writeMsg(messageLf)) {
				System.out.println("Disconnected Client " + ct.getUsername() + " removed from list.");
				remove(ct.id);
			}

		}
	}

	@Override
	public void displayMessage(List<ChatMessage> messageList) {
		for (ChatMessage message : messageList) {
			displayMessage(message);
		}
	}

	@Override
	public synchronized void onChatClose() {
		// I was asked to stop
		keepGoing = false;
		try {
			for (int i = 0; i < mClients.size(); ++i) {
				ClientThread client = mClients.get(i);
				client.kill();
			}
		} catch (Exception e) {
			System.out.println("Exception closing the server and clients: " + e);
		}
	}

	public synchronized void remove(int id) {
		// scan the array list until we found the Id
		for (int i = 0; i < mClients.size(); ++i) {
			ClientThread ct = mClients.get(i);
			// found it
			if (ct.id == id) {
				mClients.remove(i);
				return;
			}
		}
	}
}
