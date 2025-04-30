import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final RaftNode raftNode;

    public ClientHandler(Socket socket, RaftNode raftNode) {
        this.socket = socket;
        this.raftNode = raftNode;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Read the action type (SEND or RECEIVE)
            String action = (String) in.readObject();

            if ("SEND".equals(action)) {
                // Handle the sending of a message
                Message msg = (Message) in.readObject();
                raftNode.appendLog(msg);  // Append the message to the Raft log
                out.writeObject("OK");    // Acknowledge message reception
            } else if ("RECEIVE".equals(action)) {
                // Handle retrieving messages for a user
                String user = (String) in.readObject();
                List<Message> messages = raftNode.getMessagesForUser(user);  // Fetch messages for the user
                out.writeObject(messages);  // Send the list of messages back to the client
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();  // Handle exceptions
        }
    }
}