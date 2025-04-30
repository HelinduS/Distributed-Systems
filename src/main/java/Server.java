import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static RaftNode raftNode;
    private static boolean isBackup = false; // Flag to check if the server is a backup
    private static final List<String> peers = Arrays.asList("localhost:12345", "localhost:12346"); // List of peer nodes

    public static void main(String[] args) {
        int port = 12345; // Default port for the primary server
        if (args.length > 0 && "backup".equalsIgnoreCase(args[0])) {
            port = 12346; // If the argument is "backup", use the backup port
            isBackup = true; // Mark this server as a backup
        }

        String nodeId = "Node-" + port; // Generate a unique node ID based on the port
        raftNode = new RaftNode(nodeId, port, peers); // Initialize the Raft node
        raftNode.start(); // Start the Raft node

        System.out.println((isBackup ? "Backup" : "Primary") + " " + nodeId + " running on port " + port);

        // Set up the server to accept incoming client connections
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept client connection
                new Thread(new ClientHandler(clientSocket)).start(); // Handle client in a new thread
            }
        } catch (IOException e) {
            System.err.println("Port already in use: " + port); // Error if port is already in use
        }
    }

    // Inner class to handle client requests
    static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
            ) {
                String action = (String) in.readObject(); // Read action from client

                switch (action) {
                    case "SEND":
                        Message msg = (Message) in.readObject(); // Read the message to send
                        raftNode.appendLog(msg); // Append message to Raft log
                        if (!isBackup) {
                            replicateToBackup(msg); // Replicate the message to the backup server if this is the primary
                        }
                        out.writeObject("OK"); // Respond to client that the message was successfully sent
                        break;

                    case "RECEIVE":
                        String user = (String) in.readObject(); // Get the user requesting messages
                        List<Message> messages = raftNode.getMessagesForUser(user); // Retrieve messages for the user
                        out.writeObject(messages); // Send messages back to the client
                        break;

                    case "REPLICATE":
                        // Handle replication from primary to backup server
                        if (isBackup) {
                            Message replicatedMsg = (Message) in.readObject(); // Read the replicated message
                            raftNode.appendLog(replicatedMsg); // Append the message to the backup's log
                            out.writeObject("ACK"); // Acknowledge successful replication
                        } else {
                            out.writeObject("IGNORED"); // Ignore replication request if not a backup
                        }
                        break;

                    default:
                        out.writeObject("INVALID_ACTION"); // Handle invalid action request
                }
            } catch (IOException | ClassNotFoundException ignored) {
            } finally {
                try {
                    socket.close(); // Ensure the socket is closed after handling the client
                } catch (IOException ignored) {
                }
            }
        }

        // Method to replicate a message to the backup server
        private void replicateToBackup(Message msg) {
            try {
                String[] backupHostPort = peers.get(1).split(":"); // Get the backup server's host and port
                Socket backupSocket = new Socket(backupHostPort[0], Integer.parseInt(backupHostPort[1])); // Connect to the backup server

                ObjectOutputStream out = new ObjectOutputStream(backupSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(backupSocket.getInputStream());

                out.writeObject("REPLICATE"); // Send replicate request to backup
                out.writeObject(msg); // Send the message to be replicated

                Object response = in.readObject(); // Wait for acknowledgment from the backup server

                backupSocket.close(); // Close the connection to the backup server
            } catch (Exception e) {
                System.err.println("Backup replication failed: " + e.getMessage()); // Handle replication failure
            }
        }
    }
}