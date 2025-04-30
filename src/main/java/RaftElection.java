import java.util.*;

public class RaftElection {
    private final List<RaftNode> nodes; // List of all nodes in the Raft cluster
    private final Random random = new Random(); // Random generator to simulate vote decisions

    // Constructor to initialize the election with a list of nodes
    public RaftElection(List<RaftNode> nodes) {
        this.nodes = nodes;
    }

    // Method to start the election process for a candidate
    public void startElection(RaftNode candidate) {
        candidate.setState(RaftState.CANDIDATE); // Set the candidate state
        candidate.setCurrentTerm(candidate.getCurrentTerm() + 1); // Increment the term

        int votes = 1; // Candidate votes for itself initially
        // Iterate through all nodes to solicit votes
        for (RaftNode node : nodes) {
            if (!node.getId().equals(candidate.getId())) { // Skip the candidate itself
                if (random.nextBoolean()) { // Randomly decide if a node votes for the candidate
                    votes++;
                    System.out.println(node.getId() + " votes for " + candidate.getId());
                } else {
                    System.out.println(node.getId() + " denies vote to " + candidate.getId());
                }
            }
        }

        // If the candidate gets more than half the votes, it becomes the leader
        if (votes > nodes.size() / 2) {
            candidate.setState(RaftState.LEADER); // Candidate is now the leader
            candidate.setLeaderId(candidate.getId()); // Set leader's ID
            System.out.println(candidate.getId() + " is elected leader.");
            // Notify all other nodes about the new leader
            for (RaftNode node : nodes) {
                if (!node.getId().equals(candidate.getId())) {
                    node.setLeaderId(candidate.getId()); // Set the new leader on all nodes
                }
            }
        } else {
            candidate.setState(RaftState.FOLLOWER); // Candidate remains a follower
            System.out.println(candidate.getId() + " failed to become leader.");
        }
    }
}