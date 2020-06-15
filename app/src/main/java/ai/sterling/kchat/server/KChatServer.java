package ai.sterling.kchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ai.sterling.kchat.domain.chat.model.ChatMessage;
import ai.sterling.kchat.multicore.model.SampleKt;
import ai.sterling.kchat.server.view.ChatWindow;
import ai.sterling.kchat.server.view.ChatWindow.OnChatCloseListener;
import ai.sterling.logger.KLogger;

public class KChatServer implements OnChatCloseListener, MessageListener {

	// an ArrayList to keep the list of the Client
	private final ArrayList<ClientThread> clientThreadList;

	// a unique ID for each connection
	public static int uniqueId;

	// flag for stopping server
	private boolean keepGoing = true;

	// the port number to listen for connection
	private final int port;

	public KChatServer(int port) {
		this.port = port;

		// Client list
		clientThreadList = new ArrayList<>();
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
			System.out.println("exception getting PORT environment variable, " + e.getMessage());
		}

		KChatServer server = new KChatServer(portNumber);
		server.start();
	}

	public void start() {
		Socket clientSocket;

		try {
			ServerSocket serverSocket = new ServerSocket(port);

			ChatWindow chatWindow = new ChatWindow();
			chatWindow.open(KChatServer.this, this);
			chatWindow.displayMessage(new ChatMessage(
					99,
					"user1",
					ChatMessage.Companion.getMESSAGE(),
					SampleKt.hello(),
					new Date().getTime()
			));

			System.out.println("Server started. Listening to the port " + port + ". Waitng for a client to connect.");

			// infinite loop to wait for connections
			while (keepGoing) {
				clientSocket = serverSocket.accept();

				System.out.println("Client connected on port " + port);

				// if was asked to stop
				if (!keepGoing) {
					break;
				}

				ArrayList<MessageListener> listeners = new ArrayList<>();
				listeners.add(this);
				listeners.add(chatWindow);
				ClientThread receiver = new ClientThread(clientSocket, listeners);

				clientThreadList.add(receiver);
				receiver.start();
			}

			serverSocket.close();
		} catch (IOException e) {
			System.out.println("Could not listen on port: " + port);
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void displayMessage(ChatMessage message) {
		System.out.println("display message to clients");
		KLogger.d(() ->
			"display message to clients"
		);
		// loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for (int i = clientThreadList.size(); --i >= 0;) {
			ClientThread ct = clientThreadList.get(i);

			// try to write to the Android Client if it fails remove it from the list
			if (!ct.writeMsg(message)) {
				System.out.println("Disconnected Client " + ct.getUsername() + " removed from list.");
				KLogger.d(() ->
					"Disconnected Client " + ct.getUsername() + " removed from list."
				);
				remove(ct.getId());
			}

		}
	}

	@Override
	public void displayMessage(List<ChatMessage> messageList) {
		if (messageList != null && messageList.isEmpty()) {
			return;
		}
		for (ChatMessage message : messageList) {
			displayMessage(message);
		}
	}

	@Override
	public synchronized void onChatClose() {
		// I was asked to stop
		keepGoing = false;
		try {
			for (ClientThread client : clientThreadList) {
				client.kill();
			}
		} catch (Exception e) {
			System.out.println("Exception closing the server and clients: " + e.getMessage());
			KLogger.e(e, () ->
					"Exception closing the server and clients: " + e.getMessage()
			);
		}
	}

	public synchronized void remove(int id) {
		System.out.println("remove client with id=" + id);
		KLogger.d(() ->
				"remove client with id=" + id
		);
		// scan the array list until we found the Id
		for (int i = 0; i < clientThreadList.size(); ++i) {
			ClientThread ct = clientThreadList.get(i);
			// found it
			if (ct.getId() == id) {
				ct.kill();
				clientThreadList.remove(i);
				return;
			}
		}
	}
}
