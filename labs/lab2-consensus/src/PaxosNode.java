import java.util.HashMap;
import java.util.Map;

public class PaxosNode {

    private final String id;

    // Log position -> chosen value (bank balance after transaction)
    private final Map<Integer, Integer> log = new HashMap<>();

    // Acceptor state per log position:
    // highest promised proposal number
    private final Map<Integer, Integer> promisedN = new HashMap<>();
    // accepted proposal number/value
    private final Map<Integer, Integer> acceptedN = new HashMap<>();
    private final Map<Integer, Integer> acceptedValue = new HashMap<>();

    // Simulation: if lagging, node does not learn commits broadcasted by others
    private boolean lagging = false;

    // Local proposer counter to generate unique proposal numbers
    private int proposerCounter = 0;

    public PaxosNode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public void setLagging(boolean lagging) {
        this.lagging = lagging;
    }

    public boolean isLagging() {
        return lagging;
    }

    public boolean hasLogValue(int pos) {
        return log.containsKey(pos);
    }

    public Integer getLogValue(int pos) {
        return log.get(pos);
    }

    public int getLastChosenBalance() {
        int last = -1;
        int lastVal = 0;
        for (Map.Entry<Integer, Integer> e : log.entrySet()) {
            if (e.getKey() > last) {
                last = e.getKey();
                lastVal = e.getValue();
            }
        }
        return lastVal;
    }

    public int getNextLogPos() {
        int max = -1;
        for (int k : log.keySet()) max = Math.max(max, k);
        return max + 1;
    }

    // Acceptor API 

    public PaxosMessageTypes.Promise onPrepare(int pos, int n) {
        int currentPromised = promisedN.getOrDefault(pos, -1);
        if (n > currentPromised) {
            promisedN.put(pos, n);
            Integer aN = acceptedN.get(pos);
            Integer aV = acceptedValue.get(pos);
            return new PaxosMessageTypes.Promise(true, n, aN, aV);
        } else {
            // reject
            Integer aN = acceptedN.get(pos);
            Integer aV = acceptedValue.get(pos);
            return new PaxosMessageTypes.Promise(false, currentPromised, aN, aV);
        }
    }

    public PaxosMessageTypes.Accepted onAccept(int pos, int n, int value) {
        int currentPromised = promisedN.getOrDefault(pos, -1);
        if (n >= currentPromised) {
            promisedN.put(pos, n);
            acceptedN.put(pos, n);
            acceptedValue.put(pos, value);
            return new PaxosMessageTypes.Accepted(true, n, value);
        } else {
            return new PaxosMessageTypes.Accepted(false, currentPromised, value);
        }
    }

    //Learner (commit)

    public void learnChosenValue(int pos, int value) {
        if (lagging) return; // simulate late server
        log.put(pos, value);
        // once chosen, we can clear acceptor accepted state for that position (optional)
    }

    public void forceLearnChosenValue(int pos, int value) {
        // used when catching up
        log.put(pos, value);
    }

    //Proposer helpers

    public int nextProposalNumber() {
        // Make proposal numbers unique across nodes by embedding node id hash.
        // Not required in real Paxos, but avoids clashes in this simulation.
        proposerCounter++;
        int nodeSalt = Math.abs(id.hashCode()) % 1000;
        return proposerCounter * 1000 + nodeSalt;
    }

    @Override
    public String toString() {
        return "Node " + id + " (lagging=" + lagging + ")";
    }

    public String logToString() {
        StringBuilder sb = new StringBuilder();
        int max = -1;
        for (int k : log.keySet()) max = Math.max(max, k);
        for (int i = 0; i <= max; i++) {
            sb.append("pos ").append(i).append(": ");
            if (log.containsKey(i)) sb.append("$").append(log.get(i));
            else sb.append("(empty)");
            sb.append("\n");
        }
        return sb.toString();
    }
}
