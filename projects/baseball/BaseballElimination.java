import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseballElimination {

    /** the number of teams */
    private final int n;
    private final List<String> teams;
    private final HashMap<String, Integer> map;
    private final int[] wins;
    private final int[] losses;
    private final int[] remainings;
    private final int[][] games;
    private final boolean[] isSolved;
    private final HashMap<String, List<String>> certification;
    private final boolean[] isEliminated;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        In in = new In(filename);
        n = in.readInt();
        teams = new ArrayList<>();
        map = new HashMap<>();
        wins = new int[n];
        losses = new int[n];
        remainings = new int[n];
        games = new int[n][n];
        isSolved = new boolean[n];
        certification = new HashMap<>();
        isEliminated = new boolean[n];

        for (int i = 0; i < n; i++) {
            String team = in.readString();
            teams.add(team);
            map.put(team, i);
            wins[i] = in.readInt();
            losses[i] = in.readInt();
            remainings[i] = in.readInt();
            for (int j = 0; j < n; j++) {
                games[i][j] = in.readInt();
            }
        }
    }

    private void doElimination(int id) {
        trivialCase(id);
        if (!isEliminated[id]) {
            nontrivialCase(id);
        }
        isSolved[id] = true;
    }

    private void trivialCase(int id) {
        // if w[x] + r[x] < w[i], then team x is mathematically eliminated.
        for (int i = 0; i < n; i++) {
            if (wins[id] + remainings[id] < wins[i]) {
                isEliminated[id] = true;
                certification.put(teams.get(id), Collections.singletonList(teams.get(i)));
                return;
            }
        }
    }

    private void nontrivialCase(int id) {
        FlowNetwork network = createFlowNetwork(id);
        FordFulkerson ff = new FordFulkerson(network, 0, (n - 1) * (n - 2) / 2 + n);

        // sum all the remaining games except team x
        int sum = 0;
        for (int i = 0; i < n; i++) {
            if (i != id) {
                for (int j = i + 1; j < n; j++) {
                    if (j != id) {
                        sum = sum + games[i][j];
                    }
                }
            }
        }
        // if all edges in the max flow that are pointing from s are full
        // then no team wins more games than x and x can't be eliminated
        if (sum == ff.value()) {
            isEliminated[id] = false;
        } else {
            isEliminated[id] = true;
            findCertifications(ff, network, id);
        }
    }

    // if some edges pointing from s are not full,
    // then there is no scenario in which team x can win the division.
    // x can be eliminated and teams which the non-full edges pointing to are the certifications.
    private void findCertifications(FordFulkerson ff, FlowNetwork network, int id) {
        Set<Integer> set = new HashSet<>();
        for (FlowEdge edge : network.adj(0)) {
            int v = edge.to();
            if (ff.inCut(v)) {
                for (FlowEdge e : network.adj(v)) {
                    set.add(e.to() - (n - 1) * (n - 2) / 2 - 1);
                }
            }
        }
        List<String> certifications = new ArrayList<>();
        for (int num : set) {
            // note: set also contains edge between game verticals and s
            // so the redundant verticals must be removed
            if (num >= 0) {
                int index = num < id ? num : num + 1;
                certifications.add(teams.get(index));
            }
        }
        certification.put(teams.get(id), certifications);
    }

    /**
     * verticals:
     * 0 -> s   1~(n - 1) * (n - 2) / 2 -> game verticals
     * (n - 1) * (n - 2) / 2 + 1~(n - 1) * (n - 2) / 2 + n - 1 -> team verticals
     * (n - 1) * (n - 2) / 2 + n -> t
     */
    private FlowNetwork createFlowNetwork(int id) {
        FlowNetwork network = new FlowNetwork(2 + (n - 1) + (n - 1) * (n - 2) / 2);

        // create edges between s and game verticals and between game verticals and team verticals
        int countEdge = 1;
        for (int i = 0; i < n; i++) {
            if (i != id) {
                int v = i < id ? i : i - 1;
                for (int j = i + 1; j < n; j++) {
                    if (j != id) {
                        // set game vertex i-j capacity to games[i][j]
                        network.addEdge(new FlowEdge(0, countEdge, games[i][j]));
                        int w = j < id ? j : j - 1;
                        // not restrict the amount of flow on edges between game verticals and team verticals
                        network.addEdge(new FlowEdge(countEdge,
                                (n - 1) * (n - 2) / 2 + v + 1, Double.POSITIVE_INFINITY));
                        network.addEdge(new FlowEdge(countEdge,
                                (n - 1) * (n - 2) / 2 + w + 1, Double.POSITIVE_INFINITY));
                        countEdge = countEdge + 1;
                    }
                }
            }
        }

        // create edges between team verticals and t
        int count = 1;
        for (int i = 0; i < n; i++) {
            if (i != id) {
                // set capacity wins[x] + remainings[x] - wins[i] to know if there is some way of completing all the games
                // so that team x ends up winning at least as many games as team i
                network.addEdge(new FlowEdge((n - 1) * (n - 2) / 2 + count,
                        (n - 1) * (n - 2) / 2 + n, wins[id] + remainings[id] - wins[i]));
                count = count + 1;
            }
        }
        return network;
    }

    // number of teams
    public int numberOfTeams() {
        return n;
    }

    // all teams
    public Iterable<String> teams() {
        return teams;
    }

    // number of wins for given team
    public int wins(String team) {
        checkInput(team);
        return wins[map.get(team)];
    }

    // number of losses for given team
    public int losses(String team) {
        checkInput(team);
        return losses[map.get(team)];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        checkInput(team);
        return remainings[map.get(team)];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        checkInput(team1);
        checkInput(team2);
        return games[map.get(team1)][map.get(team2)];
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        checkInput(team);
        int id = map.get(team);
        if (!isSolved[id]) {
            doElimination(id);
        }
        return isEliminated[id];
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        checkInput(team);
        int id = map.get(team);
        if (!isSolved[id]) {
            doElimination(id);
        }
        return isEliminated[id] ? certification.get(team) : null;
    }

    private void checkInput(String team) {
        if (team == null) {
            throw new IllegalArgumentException();
        }
        if (!teams.contains(team)) {
            throw new IllegalArgumentException();
        }
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
