import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private static final String PRIMARY_SERVER = "localhost:12345"; // Primary server address
    private static final String BACKUP_SERVER = "localhost:12346"; // Backup server address
    private static final List<String> servers = Arrays.asList(PRIMARY_SERVER, BACKUP_SERVER);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        while (true) {
            System.out.println("\n1. Send message\n2. Check messages\n3. Exit");
            System.out.print("Choose option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    sendMessage(username, scanner);
                    break;
                case 2:
                    checkMessages(username);
                    break;
                case 3:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }

    private static void sendMessage(String username, Scanner scanner) {
        System.out.print("Enter recipient: ");
        String recipient = scanner.nextLine();

        System.out.print("Enter message: ");
        String content = scanner.nextLine();

        Message message = new Message(username, recipient, content);

        // Attempt to send the message to the available servers
        for (String server : servers) {
            String[] serverInfo = server.split(":");
            String host = serverInfo[0];
            int port = Integer.parseInt(serverInfo[1]);

            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                out.writeObject("SEND");
                out.writeObject(message);
                System.out.println("Message sent to " + recipient);
                return; // Stop after successfully sending the message
            } catch (IOException e) {
                System.out.println("Error connecting to server " + server + ": " + e.getMessage());
            }
        }

        System.out.println("Failed to send message. Both servers are down.");
    }

    @SuppressWarnings("unchecked")
    private static void checkMessages(String username) {
        for (String server : servers) {
            String[] serverInfo = server.split(":");
            String host = serverInfo[0];
            int port = Integer.parseInt(serverInfo[1]);

            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject("RECEIVE");
                out.writeObject(username);

                List<Message> messages = (List<Message>) in.readObject();

                if (messages.isEmpty()) {
                    System.out.println("No new messages.");
                } else {
                    for (Message msg : messages) {
                        System.out.println(msg);
                    }
                }
                return;
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error connecting to server " + server + ": " + e.getMessage());
            }
        }

        System.out.println("Failed to retrieve messages. Both servers are down.");
    }
}