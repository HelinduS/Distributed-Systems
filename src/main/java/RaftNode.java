import java.util.*;
import java.util.concurrent.locks.*;

public class RaftNode {
    private final String id; // Unique identifier for the node
    private final int port; // Port the node listens on
    private final List<String> peers; // List of peer nodes in the cluster
    private List<Message> log = new ArrayList<>(); // Log of messages for this node
    private RaftState state = RaftState.FOLLOWER; // Initial state is FOLLOWER
    private int currentTerm = 0; // Current term of the node
    private String leaderId = null; // ID of the current leader (if any)
    private final Lock lock = new ReentrantLock(); // Lock for thread safety when accessing the log

    // Constructor to initialize the RaftNode with id, port, and peer list
    public RaftNode(String id, int port, List<String> peers) {
        this.id = id;
        this.port = port;
        this.peers = peers;
    }

    // Placeholder method to start the node (e.g., for election or log replication)
    public void start() {
        System.out.println(id + " started on port " + port);
    }

    // Method to append a message to the node's log with thread safety
    public void appendLog(Message message) {
        lock.lock(); // Acquire the lock before modifying the log
        try {
            log.add(message); // Add the message to the log
            // System.out.println("Message appended to log: " + message.getContent()); // Optional debug line
        } finally {
            lock.unlock(); // Ensure the lock is released after the operation
        }
    }

    // Method to retrieve all messages for a specific user
    public List<Message> getMessagesForUser(String user) {
        List<Message> userMessages = new ArrayList<>();
        lock.lock(); // Acquire the lock before accessing the log
        try {
            // Iterate through the log and add messages for the user to the result list
            for (Message msg : log) {
                if (msg.getRecipient().equals(user)) {
                    userMessages.add(msg);
                }
            }
        } finally {
            lock.unlock(); // Ensure the lock is released after the operation
        }
        return userMessages;
    }

    // Getter and setter methods for the RaftNode properties
    public String getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public void setState(RaftState state) {
        this.state = state;
    }

    public RaftState getState() {
        return state;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setCurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }
}