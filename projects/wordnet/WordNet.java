
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.DirectedCycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class WordNet {

    /**
     * map the word with all positions in graph
     * Note: a synset contains more than one word.
     * and one word can appear more than once in graph in different position
     * for one word can have more than one meaning.
     * */
    private final HashMap<String, List<Integer>> wordToInt;

    /** record all the synsets i.e. all the verticals in graph */
    private final String[] allSetWords;

    private final int numV;

    private String lastA;
    private String lastB;

    private List<Integer> lastIdsA;
    private List<Integer> lastIdsB;

    private final SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {

        if (synsets == null || hypernyms == null) {
            throw new IllegalArgumentException();
        }

        wordToInt = new HashMap<>();
        In in = new In(synsets);
        ArrayList<String> tmpWord = new ArrayList<>();
        
        // read the synsets file
        while (in.hasNextLine()) {
            String line = in.readLine();
            String[] strings = line.split(",");
            tmpWord.add(strings[1]);
            String[] words = strings[1].split(" ");

            for (String word : words) {
                if (!wordToInt.containsKey(word)) {
                    List<Integer> list = new LinkedList<>();
                    list.add(Integer.parseInt(strings[0]));
                    wordToInt.put(word, list);
                } else {
                    wordToInt.get(word).add(Integer.parseInt(strings[0]));
                }
            }
        }
        Digraph digraph = new Digraph(tmpWord.size());
        numV = tmpWord.size();
        allSetWords = tmpWord.toArray(new String[0]);

        // read the hypernyms file
        in = new In(hypernyms);
        while (in.hasNextLine()) {
            String line = in.readLine();
            String[] strings = line.split(",");
            for (int i = 1; i < strings.length; i++) {
                digraph.addEdge(Integer.parseInt(strings[0]), Integer.parseInt(strings[i]));
            }
        }
        if (hasCircle(digraph)) {
            throw new IllegalArgumentException();
        }
        if (!isRootedDAG(digraph)) {
            throw new IllegalArgumentException();
        }
        sap = new SAP(digraph);
    }

    private boolean hasCircle(Digraph digraph) {
        DirectedCycle directedCycle = new DirectedCycle(digraph);
        return directedCycle.hasCycle();
    }

    private boolean isRootedDAG(Digraph digraph) {
        int count = 0;
        for (int i = 0; i < numV; i++) {
            if (digraph.outdegree(i) == 0) {
                count = count + 1;
            }
            if (count > 1) {
                return false;
            }
        }
        return true;
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return wordToInt.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new IllegalArgumentException();
        }
        return wordToInt.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (nounA == null || nounB == null) {
            throw new IllegalArgumentException();
        }

        if (nounA.equals(lastA) && nounB.equals(lastB)) {
            return sap.length(lastIdsA, lastIdsB);
        }

        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }

        lastA = nounA;
        lastB = nounB;
        lastIdsA = wordToInt.get(nounA);
        lastIdsB = wordToInt.get(nounB);

        if (lastIdsA.size() == 1 && lastIdsB.size() == 1) {
            return sap.length(lastIdsA.get(0), lastIdsB.get(0));
        }

        // note the word can appear more than once in graph
        // so the wordToInt.get() may return a iterable set
        return sap.length(lastIdsA, lastIdsB);
    }



    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in the shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (nounA == null || nounB == null) {
            throw new IllegalArgumentException();
        }

        if (nounA.equals(lastA) && nounB.equals(lastB)) {
            return allSetWords[sap.ancestor(lastIdsA, lastIdsB)];
        }

        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }

        lastA = nounA;
        lastB = nounB;
        lastIdsA = wordToInt.get(nounA);
        lastIdsB = wordToInt.get(nounB);

        if (lastIdsA.size() == 1 && lastIdsB.size() == 1) {
            return allSetWords[sap.ancestor(lastIdsA.get(0), lastIdsB.get(0))];
        }

        return allSetWords[sap.ancestor(lastIdsA, lastIdsB)];
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet wordNet = new WordNet("synsets.txt", "hypernyms.txt");
        System.out.println(wordNet.distance("artichoke", "lemon_zest")); // 11
        System.out.println(wordNet.sap("artichoke", "lemon_zest")); // matter
        System.out.println(wordNet.distance("Tennessee_Williams", "family_Labiatae")); // 15

    }
}
