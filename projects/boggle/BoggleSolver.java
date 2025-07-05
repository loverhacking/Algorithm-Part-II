import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class BoggleSolver {
    private List<String> answers;
    private int uid;
    private int[][] neighbors;
    private final int[][] neighborsRow4Col4;
    private final Set<String> allWords = new HashSet<>();
    private int m, n;
    private final TrieNode root;
    private int[] charBoard;
    private boolean[] visited;
    private final int[] charBoard4x4 = new int[16]; // pre-allocate 4x4 boards

    private static class TrieNode {
        // notice that the alphabet consists of only the 26 letters A through Z
        final TrieNode[] children = new TrieNode[26];
        boolean hasChild;
        // represent the current getAllValidWords() call id to find whether the word has been found
        int uid;
        // build string in trie node to avoid frequently building strings in dfs
        String word;
    }

    // Initializes the data structure using the given array of strings as the dictionary.
    // (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        root = new TrieNode();
        for (String word : dictionary) {
            // only store the >= 3 length word in trie to reduce judgment in dfs
            if (word.length() >= 3) insert(word);
            // store all the words in set
            allWords.add(word);
        }
        uid = 0;

        // pre-store the standard 4x4 boards neighbors
        neighborsRow4Col4 = new int[][]{{1, 4, 5}, {0, 2, 4, 5, 6}, {1, 3, 5, 6, 7}, {2, 6, 7},
                {0, 1, 5, 8, 9}, {0, 1, 2, 4, 6, 8, 9, 10}, {1, 2, 3, 5, 7, 9, 10, 11}, {2, 3, 6, 10, 11},
                {4, 5, 9, 12, 13}, {4, 5, 6, 8, 10, 12, 13, 14}, {5, 6, 7, 9, 11, 13, 14, 15}, {6, 7, 10, 14, 15},
                {8, 9, 13}, {8, 9, 10, 12, 14}, {9, 10, 11, 13, 15}, {10, 11, 14}};
    }

    private void insert(String word) {
        TrieNode node = root;

        int i = 0;
        while (i < word.length()) {
            char c = word.charAt(i);
            int idx = c - 'A';
            if (node.children[idx] == null) {
                node.children[idx] = new TrieNode();
                node.hasChild = true;
            }
            node = node.children[idx];
            if (c == 'Q') {
                i++; // Skip "Qu"
                if (i == word.length() || word.charAt(i) != 'U') {
                    // Ignore "Q" and "Qx"
                    return;
                }
            }
            i++;
        }
        node.word = word;
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        answers = new ArrayList<>();
        uid++;
        m = board.rows();
        n = board.cols();

        if (m == 4 && n == 4) {
            // deal with 4x4 board specially to pass timing test
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    char c = board.getLetter(i, j);
                    charBoard4x4[i * 4 + j] = c - 'A';
                }
            }
            neighbors = neighborsRow4Col4;
            for (int i = 0; i < 16; i++) {
                TrieNode node = root.children[charBoard4x4[i]];
                if (node != null) dfs4x4(i, node, 0);
            }
        } else {
            // deal with the general board
            charBoard = new int[m * n];
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    charBoard[i * n + j] = board.getLetter(i, j) - 'A';
                }
            }
            if (visited == null || visited.length < m * n) visited = new boolean[m * n];
            // precompute the neighbors for every element
            findNeighbors();
            for (int i = 0; i < m * n; i++) {
                TrieNode node = root.children[charBoard[i]];
                if (node != null) dfs(i, node);
            }
        }
        return answers;
    }

    private void dfs4x4(int index, TrieNode node, int visitedBit) {
        if (node.word != null && node.uid != uid) {
            answers.add(node.word);
            node.uid = uid;
        }
        // pruning: when next trie node is null, no need to dfs more
        if (!node.hasChild) return;

        // In the 4x4 board, the nodes are numbered from 0 to 15.
        // use a `int` type variable `visitedBit` to represent the visited status
        // use bit operations to set and check the visited status
        // notice that "newVisited" is equivalent to a new visited array with neighbor status modified only
        // which avoid the reset visited operations
        int newVisited = visitedBit | (1 << index); // set the location of index to be 1
        for (int neighbor : neighborsRow4Col4[index]) {
            if ((newVisited & (1 << neighbor)) != 0) continue; // check the location of index is 1
            TrieNode next = node.children[charBoard4x4[neighbor]];
            if (next != null) dfs4x4(neighbor, next, newVisited);
        }
    }

    private void dfs(int index, TrieNode node) {
        if (node.word != null && node.uid != uid) {
            answers.add(node.word);
            node.uid = uid;
        }

        // pruning: when next trie node is null, no need to dfs more
        if (!node.hasChild) return;

        visited[index] = true;
        for (int neighbor : neighbors[index]) {
            if (visited[neighbor]) continue;
            TrieNode next = node.children[charBoard[neighbor]];
            if (next != null) dfs(neighbor, next);
        }
        visited[index] = false;
    }

    private void findNeighbors() {
        neighbors = new int[m * n][];
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                int id = i * n + j;
                List<Integer> list = new ArrayList<>();
                for (int d = 0; d < 8; d++) {
                    int ni = i + dx[d], nj = j + dy[d];
                    if (ni >= 0 && ni < m && nj >= 0 && nj < n)
                        list.add(ni * n + nj);
                }
                neighbors[id] = new int[list.size()];
                for (int k = 0; k < list.size(); k++)
                    neighbors[id][k] = list.get(k);
            }
        }
    }

    public int scoreOf(String word) {
        if (!allWords.contains(word)) return 0;
        int len = word.length();
        if (len < 3) return 0;
        if (len <= 4) return 1;
        if (len == 5) return 2;
        if (len == 6) return 3;
        if (len == 7) return 5;
        return 11;
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        StdOut.println("Score = " + score);
    }
}

