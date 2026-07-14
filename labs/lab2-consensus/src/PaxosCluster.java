import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PaxosCluster {

    private final List<PaxosNode> nodes;

    public PaxosCluster(PaxosNode a, PaxosNode b, PaxosNode c) {
        nodes = new ArrayList<>();
        nodes.add(a);
        nodes.add(b);
        nodes.add(c);
    }

    public PaxosNode getById(String id) {
        for (PaxosNode n : nodes) {
            if (n.id().equalsIgnoreCase(id)) return n;
        }
        return null;
    }

    public List<PaxosNode> all() {
        return nodes;
    }

    public int majority() {
        return (nodes.size() / 2) + 1; // 2 for 3 nodes
    }

    /*
    If a proposer wants to write at pos but is missing the chosen value, 
    it can learn it from the majority .
     */
    public void catchUpFromMajority(PaxosNode requester, int pos) {
        int count = 0;
        Integer val = null;

        for (PaxosNode n : nodes) {
            Integer v = n.getLogValue(pos);
            if (v != null) {
                count++;
                if (val == null) val = v;
            }
        }

        if (count >= majority() && val != null) {
            requester.forceLearnChosenValue(pos, val);
        }
    }

    /**
    Run one Paxos instance for a given log position, trying to choose 'proposedValue'.
    Returns the chosen value if consensus reached.
     */
    public Integer runPaxosInstance(PaxosNode proposer, int pos, int proposedValue, boolean verbose) {
        int n = proposer.nextProposalNumber();

        if (verbose) {
            System.out.println("\nPaxos started by " + proposer.id() + " for log position " + pos + " with proposal n=" + n);
        }

        // Phase 1: Prepare
        List<PaxosMessageTypes.Promise> promises = new ArrayList<>();
        for (PaxosNode a : nodes) {
            PaxosMessageTypes.Promise p = a.onPrepare(pos, n);
            promises.add(p);
            if (verbose) {
                System.out.println("  Prepare -> " + a.id() + " : " + (p.promised ? "PROMISE" : "REJECT") +
                        " (promisedN=" + p.promisedN + ")");
            }
        }

        long promisedCount = promises.stream().filter(pr -> pr.promised).count();
        if (promisedCount < majority()) {
            if (verbose) System.out.println("  Not enough promises (" + promisedCount + "/" + majority() + "). Abort.");
            return null;
        }

        // Choose value: if any acceptor already accepted a value, pick the one with highest acceptedN
        Integer valueToPropose = null;
        int bestAcceptedN = -1;
        for (PaxosMessageTypes.Promise pr : promises) {
            if (pr.acceptedN != null && pr.acceptedValue != null) {
                if (pr.acceptedN > bestAcceptedN) {
                    bestAcceptedN = pr.acceptedN;
                    valueToPropose = pr.acceptedValue;
                }
            }
        }
        if (valueToPropose == null) valueToPropose = proposedValue;

        if (verbose) {
            System.out.println("  Phase 2 value selected: " + valueToPropose +
                    (valueToPropose == proposedValue ? " (client value)" : " (adopted previously accepted value)"));
        }

        // Phase 2: Accept
        List<PaxosMessageTypes.Accepted> accepted = new ArrayList<>();
        for (PaxosNode a : nodes) {
            PaxosMessageTypes.Accepted ac = a.onAccept(pos, n, valueToPropose);
            accepted.add(ac);
            if (verbose) {
                System.out.println("  Accept -> " + a.id() + " : " + (ac.accepted ? "ACCEPT" : "REJECT"));
            }
        }

        long acceptedCount = accepted.stream().filter(ac -> ac.accepted).count();
        if (acceptedCount < majority()) {
            if (verbose) System.out.println("  Not enough accepts (" + acceptedCount + "/" + majority() + "). Abort.");
            return null;
        }

        // Commit chosen value: broadcast learn (lagging nodes may ignore)
        for (PaxosNode nnode : nodes) {
            nnode.learnChosenValue(pos, valueToPropose);
        }

        if (verbose) {
            System.out.println("Consensus reached for pos " + pos + ": chosen value = $" + valueToPropose);
        }

        return valueToPropose;
    }

    public void printReplicaLogs() {
        System.out.println("\n=== Replica Logs ===");
        nodes.sort(Comparator.comparing(PaxosNode::id));
        for (PaxosNode n : nodes) {
            System.out.println("Replica " + n.id() + " (lagging=" + n.isLagging() + ")");
            System.out.print(n.logToString());
        }
    }
}
