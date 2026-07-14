import java.util.Locale;
import java.util.Scanner;

public class Main {

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java Main pow    -> run PoW benchmarks and save CSV");
        System.out.println("  java Main paxos  -> run Paxos bank simulation (terminal)");
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        if (args.length == 0) {
            printUsage();
            return;
        }

        if (args[0].equalsIgnoreCase("pow")) {
            String csvPath = "pow_results.csv";
            PowBench.runAndSaveCsv(csvPath);
            return;
        }

        if (args[0].equalsIgnoreCase("paxos")) {
            runPaxosCli();
            return;
        }

        printUsage();
    }

    private static void runPaxosCli() {
        // Create 3 servers A, B, C
        PaxosNode A = new PaxosNode("A");
        PaxosNode B = new PaxosNode("B");
        PaxosNode C = new PaxosNode("C");

        // Initial replica state : same logs on all nodes
        // Log pos 0: $100, pos 1: $150, pos 2: $130
        for (PaxosNode n : new PaxosNode[]{A, B, C}) {
            n.forceLearnChosenValue(0, 100);
            n.forceLearnChosenValue(1, 150);
            n.forceLearnChosenValue(2, 130);
        }

        PaxosCluster cluster = new PaxosCluster(A, B, C);
        BankService bank = new BankService(cluster);

        Scanner sc = new Scanner(System.in);

        System.out.println("=== Basic Paxos Bank Simulation ===");
        System.out.println("Servers: A, B, C (each acts as proposer/acceptor/learner)");
        System.out.println("Clients: 1, 2");
        System.out.println("Initial log: pos0=$100, pos1=$150, pos2=$130");
        System.out.println();

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1) Deposit");
            System.out.println("2) Withdraw");
            System.out.println("3) Toggle lagging server (simulate late catch-up)");
            System.out.println("4) Show replica logs");
            System.out.println("0) Exit");
            System.out.print("> ");

            String choice = sc.nextLine().trim();

            if (choice.equals("0")) break;

            switch (choice) {
                case "1":
                case "2": {
                    System.out.print("Client id (1 or 2): ");
                    String clientId = sc.nextLine().trim();

                    System.out.print("Send request to server (A/B/C): ");
                    String srv = sc.nextLine().trim().toUpperCase();
                    PaxosNode receiver = cluster.getById(srv);
                    if (receiver == null) {
                        System.out.println("Unknown server.");
                        break;
                    }

                    System.out.print("Amount: ");
                    int amount;
                    try {
                        amount = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount.");
                        break;
                    }
                    if (amount <= 0) {
                        System.out.println("Amount must be > 0.");
                        break;
                    }

                    int delta = choice.equals("1") ? amount : -amount;
                    bank.handleTransaction(clientId, receiver, delta);
                    break;
                }
                case "3": {
                    System.out.print("Server to toggle lagging (A/B/C): ");
                    String srv = sc.nextLine().trim().toUpperCase();
                    PaxosNode node = cluster.getById(srv);
                    if (node == null) {
                        System.out.println("Unknown server.");
                        break;
                    }
                    node.setLagging(!node.isLagging());
                    System.out.println("Server " + node.id() + " lagging set to: " + node.isLagging());
                    if (!node.isLagging()) {
                        // When coming back, catch up missing positions from majority
                        int next = node.getNextLogPos();
                        for (int p = 0; p < next; p++) {
                            if (!node.hasLogValue(p)) {
                                cluster.catchUpFromMajority(node, p);
                            }
                        }
                        System.out.println("Server " + node.id() + " caught up from majority (if needed).");
                    }
                    break;
                }
                case "4": {
                    cluster.printReplicaLogs();
                    break;
                }
                default:
                    System.out.println("Unknown option.");
            }
        }

        System.out.println("Bye.");
    }
}
