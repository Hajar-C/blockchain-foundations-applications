public class PaxosMessageTypes {
    // Simple container classes for a basic Paxos (single-decree per log position).

    public static class Promise {
        public final boolean promised;
        public final int promisedN;
        public final Integer acceptedN;     // may be null
        public final Integer acceptedValue; // may be null

        public Promise(boolean promised, int promisedN, Integer acceptedN, Integer acceptedValue) {
            this.promised = promised;
            this.promisedN = promisedN;
            this.acceptedN = acceptedN;
            this.acceptedValue = acceptedValue;
        }
    }

    public static class Accepted {
        public final boolean accepted;
        public final int n;
        public final int value;

        public Accepted(boolean accepted, int n, int value) {
            this.accepted = accepted;
            this.n = n;
            this.value = value;
        }
    }
}
