public class BankService {

    private final PaxosCluster cluster;

    public BankService(PaxosCluster cluster) {
        this.cluster = cluster;
    }

    /**
     * Client sends a deposit/withdraw request to any node (receiver).
     * Receiver becomes the proposer and tries to pass Paxos in next log position.
     */
    public void handleTransaction(String clientId, PaxosNode receiver, int deltaAmount) {
        // Determine target log position from receiver's perspective
        int pos = receiver.getNextLogPos();

        // If receiver is missing earlier chosen values, catch up from majority
        // We'll attempt catch-up on any missing position up to pos-1
        for (int p = 0; p < pos; p++) {
            if (!receiver.hasLogValue(p)) {
                cluster.catchUpFromMajority(receiver, p);
            }
        }

        int currentBalance = receiver.getLastChosenBalance();
        int newBalance = currentBalance + deltaAmount;

        System.out.println("\nClient " + clientId + " -> " + (deltaAmount >= 0 ? "Deposit" : "Withdraw")
                + " " + Math.abs(deltaAmount) + " via Server " + receiver.id());

        if (newBalance < 0) {
            System.out.println("Rejected locally: insufficient funds (current $" + currentBalance + ")");
            return;
        }

        Integer chosen = cluster.runPaxosInstance(receiver, pos, newBalance, true);
        if (chosen == null) {
            System.out.println("Transaction failed (no consensus). Try again.");
        } else {
            System.out.println("Transaction committed at log position " + pos + " with balance $" + chosen);
        }
    }
}
