
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SAP {

    /** the number of vertices in graph */
    private final int numV;

    /**
     * record the vertices distance to v and w respectively
     * v: 0 --> numV - 1
     * w: numV-1 --> 2 * numV - 1
     */
    private final int[] dist;

    /**
     * record the changed index in dis[] for every SAP calculation
     * v: 0 --> numV - 1
     * w: numV-1 --> 2 * numV - 1
     */
    private final ArrayDeque changed;

    private final ArrayDeque vQueue;
    private final ArrayDeque wQueue;

    private final int[] adj;
    private final int[] offsets;

    private int[] res;

    private Iterable<Integer> lastSetV;
    private Iterable<Integer> lastSetW;
    private int[] lastSetResult;

    private int lastV;
    private int lastW;
    private int[] lastResult;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        if (G == null) {
            throw new IllegalArgumentException();
        }

        numV = G.V();

        dist = new int[2 * numV];
        Arrays.fill(dist, -1);
        changed = new ArrayDeque(2 * numV);
        vQueue = new ArrayDeque(2 * numV);
        wQueue = new ArrayDeque(2 * numV);

        lastSetV = null;
        lastSetW = null;
        lastSetResult = null;

        lastV = -1;
        lastW = -1;
        lastResult = null;

        res = new int[]{-1, -1};

        offsets = new int[numV + 1];
        adj = new int[G.E()];
        createAdj(G);
    }

    private void createAdj(Digraph digraph) {

        int totalEdges = 0;

        for (int i = 0; i < numV; i++) {
            totalEdges += digraph.outdegree(i);
            offsets[i + 1] = totalEdges;
        }

        for (int i = 0; i < numV; i++) {
            int idx = offsets[i];
            for (int neighbor : digraph.adj(i)) {
                adj[idx++] = neighbor;
            }
        }

    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        if (v >= numV || v < 0 || w < 0 || w >= numV) {
            throw new IllegalArgumentException();
        }

        if (lastV == v && lastW == w) {
            return lastResult[0];
        }

        lastV = v;
        lastW = w;
        if (v == w) {
            lastResult = new int[]{0, v};
            return lastResult[0];
        }
        reInitiate();
        vQueue.addLast(v);
        changed.addLast(v);
        dist[v] = 0;
        wQueue.addLast(w);
        changed.addLast(numV + w);
        dist[numV + w] = 0;

        lastResult = query();
        return lastResult[0];
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {

        if (v >= numV || v < 0 || w < 0 || w >= numV) {
            throw new IllegalArgumentException();
        }

        if (lastV == v && lastW == w) {
            return lastResult[1];
        }

        lastV = v;
        lastW = w;
        if (v == w) {
            lastResult = new int[]{0, v};
            return lastResult[1];
        }
        reInitiate();

        vQueue.addLast(v);
        changed.addLast(v);
        dist[v] = 0;
        wQueue.addLast(w);
        changed.addLast(numV + w);
        dist[numV + w] = 0;

        lastResult = query();
        return lastResult[1];
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new IllegalArgumentException();
        }

        if (!v.iterator().hasNext() || !w.iterator().hasNext()) {
            return -1;
        }

        if (v.equals(lastSetV) && w.equals(lastSetW)) {
            return lastSetResult[0];
        }
        reInitiate();

        for (Integer num : v) {
            checkNum(num);
            vQueue.addLast(num);
            dist[num] = 0;
            changed.addLast(num);
        }


        for (Integer num : w) {
            checkNum(num);
            wQueue.addLast(num);
            dist[numV + num] = 0;
            changed.addLast(numV + num);
        }

        lastSetV = v;
        lastSetW = w;
        lastSetResult = query();
        return lastSetResult[0];
    }

    // a common ancestor that participates in the shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new IllegalArgumentException();
        }

        if (!v.iterator().hasNext() || !w.iterator().hasNext()) {
            return -1;
        }

        if (v.equals(lastSetV) && w.equals(lastSetW)) {
            return lastSetResult[1];
        }

        reInitiate();

        for (Integer num : v) {
            checkNum(num);
            vQueue.addLast(num);
            dist[num] = 0;
            changed.addLast(num);
        }

        for (Integer num : w) {
            checkNum(num);
            wQueue.addLast(num);
            dist[numV + num] = 0;
            changed.addLast(numV + num);
        }

        lastSetV = v;
        lastSetW = w;
        lastSetResult = query();
        return lastSetResult[1];
    }

    private int[] query() {
        int currentMinSum = -1;
        int ancestor = -1;
        while (!vQueue.isEmpty() || !wQueue.isEmpty()) {

            // deal with v
            if (!vQueue.isEmpty()) {
                res = processVertices(vQueue.removeFirst(), currentMinSum, ancestor, vQueue, 0);
                currentMinSum = res[0];
                ancestor = res[1];
            }

            // deal with w
            if (!wQueue.isEmpty()) {
                // deal with w
                res = processVertices(wQueue.removeFirst(), currentMinSum, ancestor, wQueue, 1);
                currentMinSum = res[0];
                ancestor = res[1];
            }
        }
        return res;
    }

    private int[] processVertices(int node, int currentMinSum, int ancestor, ArrayDeque q, int sign) {

        int index = node + numV * sign;
        int indexOther = node + numV * (1 - sign);
        int total;

        if (dist[indexOther] != -1) {
            total = dist[index] + dist[indexOther];
            if (currentMinSum == -1 || total < currentMinSum) {
                ancestor = node;
                currentMinSum = total;
            }
        }

        int indexParent, parent;
        int start, end;

        if (currentMinSum == -1 || dist[index] < currentMinSum) {
            start = offsets[node];
            end = offsets[node + 1];
            for (int i = start; i < end; i++) {
                parent = adj[i];
                indexParent = parent + numV * sign;
                if (dist[indexParent] == -1) {
                    changed.addLast(indexParent);
                    dist[indexParent] = dist[index] + 1;
                    q.addLast(parent);
                }
            }
        }
        return new int[]{currentMinSum, ancestor};
    }

    private void reInitiate() {
        while (!changed.isEmpty()) {
            dist[changed.removeFirst()] = -1;
        }
    }

    private void checkNum(Integer num) {
        if (num == null) {
            throw new IllegalArgumentException();
        }
        if (num < 0 || num >= numV) {
            throw new IllegalArgumentException();
        }
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length   = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}

