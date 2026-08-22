package javaswing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {
    private static final int PORT = 12345;
    
    // Thread-safe map to hold active clients: Username -> ClientHandler
    private static final Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();
    
    // Thread-safe list to manage client broadcast list
    private static final List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("💬 Starting Chat Server on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection received from " + socket.getRemoteSocketAddress());
                
                ClientHandler handler = new ClientHandler(socket);
                clientHandlers.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Read register command/username first
                String registerMsg = in.readLine();
                if (registerMsg != null && registerMsg.startsWith("REGISTER:")) {
                    String candidate = registerMsg.substring(9).trim();
                    if (!candidate.isEmpty()) {
                        this.username = candidate;
                        activeClients.put(this.username, this);
                        System.out.println("User registered: " + this.username);
                    }
                }

                if (this.username == null) {
                    sendMessage("SYSTEM: Registration failed; closing connection.");
                    return;
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.trim().isEmpty()) continue;
                    
                    // Handle private messages (FORMAT -> PRIVATE:recipient:message)
                    if (message.startsWith("PRIVATE:")) {
                        handlePrivateMessage(message);
                    } else {
                        // Broadcast public message
                        broadcastMessage(this.username + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + (username != null ? username : socket.getRemoteSocketAddress()));
            } finally {
                cleanup();
            }
        }

        private void handlePrivateMessage(String rawMessage) {
            String[] parts = rawMessage.split(":", 3);
            if (parts.length == 3) {
                String recipient = parts[1].trim();
                String msgContent = parts[2];

                ClientHandler recipientHandler = activeClients.get(recipient);
                if (recipientHandler != null) {
                    recipientHandler.sendMessage("DIRECT:" + this.username + ":" + msgContent);
                } else {
                    sendMessage("SYSTEM: User '" + recipient + "' is currently offline.");
                }
            }
        }

        private void broadcastMessage(String message) {
            for (ClientHandler client : clientHandlers) {
                client.sendMessage(message);
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        private void cleanup() {
            if (username != null) {
                activeClients.remove(username);
            }
            clientHandlers.remove(this);
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}