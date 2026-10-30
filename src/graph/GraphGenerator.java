package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// makes random graphs, same seed = same graph
public class GraphGenerator {

    // connected undirected graph: first a random tree, then extra edges
    public static Graph connectedUndirected(int n, int m, long seed) {
        Random rnd = new Random(seed);
        Graph g = new Graph(n, false);
        Set<Long> used = new HashSet<>();
        for (int i = 1; i < n; i++) {
            int j = rnd.nextInt(i);          // connect i to some earlier vertex
            g.addEdge(i, j, 1 + rnd.nextInt(100));
            used.add(key(i, j, false, n));
        }
        while (g.edges.size() < m) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            if (a == b || used.contains(key(a, b, false, n))) continue;
            used.add(key(a, b, false, n));
            g.addEdge(a, b, 1 + rnd.nextInt(100));
        }
        return g;
    }

    // directed acyclic graph: edges only go from smaller to bigger number
    public static Graph dag(int n, int m, long seed) {
        Random rnd = new Random(seed);
        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        while (g.edges.size() < m) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            if (a == b) continue;
            int from = Math.min(a, b);
            int to = Math.max(a, b);
            if (used.contains(key(from, to, true, n))) continue;
            used.add(key(from, to, true, n));
            g.addEdge(from, to, 1 + rnd.nextInt(100));
        }
        return g;
    }

    // random directed graph, edges in any direction (so it has cycles)
    public static Graph directed(int n, int m, long seed) {
        Random rnd = new Random(seed);
        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        while (g.edges.size() < m) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            if (a == b || used.contains(key(a, b, true, n))) continue;
            used.add(key(a, b, true, n));
            g.addEdge(a, b, 1 + rnd.nextInt(100));
        }
        return g;
    }

    // unique number for a pair of vertices
    private static long key(int a, int b, boolean directed, int n) {
        if (!directed && a > b) {
            int t = a;
            a = b;
            b = t;
        }
        return (long) a * n + b;
    }
}
