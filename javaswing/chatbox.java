package javaswing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.text.DefaultCaret;

public class chatbox extends JFrame {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 12345;

    private final String currentUsername;
    private final String targetRecipient; // null for public chat room, or specific username for private 1-on-1 chat

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean isRunning = true;

    public chatbox(String currentUsername, String targetRecipient) {
        this.currentUsername = currentUsername;
        this.targetRecipient = targetRecipient;

        initUI();
        connectToServer();
    }

    private void initUI() {
        String title = (targetRecipient == null || targetRecipient.isEmpty()) 
                ? "Public Lounge Chat - " + currentUsername 
                : "Private Chat with " + targetRecipient + " (" + currentUsername + ")";
        
        setTitle(title);
        setSize(450, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Chat text area configuration
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Enable auto-scrolling to the bottom when new messages arrive
        DefaultCaret caret = (DefaultCaret) chatArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for text field and send button
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageField.addActionListener(e -> sendMessage());

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        sendButton.addActionListener(e -> sendMessage());

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Clean up socket resources on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeConnection();
            }
        });
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Register client username on connection
                out.println("REGISTER:" + currentUsername);

                appendMessage("System: Connected to chat server.");

                // Continuous incoming message listener thread
                String incomingMessage;
                while (isRunning && (incomingMessage = in.readLine()) != null) {
                    processIncomingMessage(incomingMessage);
                }
            } catch (IOException e) {
                if (isRunning) {
                    appendMessage("System: Unable to connect to chat server.");
                }
            } finally {
                closeConnection();
            }
        }).start();
    }

    private void processIncomingMessage(String message) {
        if (message.startsWith("DIRECT:")) {
            // Format: DIRECT:sender:content
            String[] parts = message.split(":", 3);
            if (parts.length == 3) {
                String sender = parts[1];
                String text = parts[2];

                // Render private message if inside private conversation window with sender
                if (targetRecipient != null && targetRecipient.equalsIgnoreCase(sender)) {
                    appendMessage(sender + " (Private): " + text);
                } else if (targetRecipient == null) {
                    // Fallback preview for private message inside main lounge frame
                    appendMessage("[Private from " + sender + "]: " + text);
                }
            }
        } else if (message.startsWith("SYSTEM:")) {
            appendMessage("System: " + message.substring(7));
        } else {
            // Render public message if currently inside public chat room
            if (targetRecipient == null || targetRecipient.isEmpty()) {
                appendMessage(message);
            }
        }
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || out == null) return;

        if (targetRecipient != null && !targetRecipient.isEmpty()) {
            // Send Private Message
            out.println("PRIVATE:" + targetRecipient + ":" + text);
            appendMessage("You (to " + targetRecipient + "): " + text);
        } else {
            // Send Public Message
            out.println(text);
        }

        messageField.setText("");
    }

    private void appendMessage(String message) {
        // Ensure UI updates strictly run on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    private synchronized void closeConnection() {
        isRunning = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing ChatBox resources: " + e.getMessage());
        }
    }
}