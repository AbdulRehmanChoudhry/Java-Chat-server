import java.io.*;
import java.net.*;
import java.util.*;

public class NewServer {
    private ServerSocket serverSocket = null;
    private Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());

    public NewServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started. Listening on port " + port);

            while (true) {
                System.out.println("Waiting for a client ...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected!");

                // Start a new thread for the client
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    // Add a client to the list
    public synchronized void addClient(String name, ClientHandler clientHandler) {
        clients.put(name, clientHandler);
        System.out.println(name + " joined the chat.");
        broadcast("Server: " + name + " has joined the chat.", null);
    }

    // Remove a client from the list
    public synchronized void removeClient(String name) {
        clients.remove(name);
        System.out.println(name + " left the chat.");
        broadcast("Server: " + name + " has left the chat.", null);
    }

    // Broadcast a message to all clients except the sender
    public synchronized void broadcast(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clients.values()) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    public static void main(String[] args) {
        new Server(4000);
    }
}

// A separate thread to handle client communication
class ClientHandler extends Thread {
    private Socket socket;
    private NewServer server;
    private DataInputStream in;
    private DataOutputStream out;
    private String clientName;

    public ClientHandler(Socket socket, NewServer server) {
            this.socket = socket;
            this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            // Ask for the client's name
            out.writeUTF("Enter your name: ");
            clientName = in.readUTF();

            // Add client to the server
            server.addClient(clientName, this);

            String message;
            while (true) {
                message = in.readUTF();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
                System.out.println(clientName + ": " + message);
                server.broadcast(clientName + ": " + message, this);
            }
        } catch (IOException e) {
            System.out.println("Connection closed with " + clientName);
        } finally {
            try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                System.out.println("Error closing connection with " + clientName);
            }
            server.removeClient(clientName);
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Error sending message to " + clientName);
        }
    }
}
