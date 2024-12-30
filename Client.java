import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream out;

    public Client(String address, int port) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected to the server");

            input = new DataInputStream(System.in);
            out = new DataOutputStream(socket.getOutputStream());
            DataInputStream serverIn = new DataInputStream(socket.getInputStream());

            // Start a thread to listen for messages from the server
            new Thread(() -> {
                try {
                    while (true) {
                        String message = serverIn.readUTF();
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from the server.");
                }
            }).start();

            // Send messages to the server
            System.out.println("Enter your name: ");
            String name = input.readLine();
            out.writeUTF(name);

            String message = "";
            while (!message.equalsIgnoreCase("exit")) {
                message = input.readLine();
                out.writeUTF(message);
            }
        } catch (IOException e) {
            System.out.println("Error connecting to the server: " + e.getMessage());
        } finally {
            try {
                input.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new Client("127.0.0.1", 4000);
    }
}
