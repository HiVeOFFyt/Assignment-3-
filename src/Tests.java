import connectivity.SCC;
import connectivity.TopoResult;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;
import java.util.ArrayList;
import java.util.List;

// simple tests without JUnit, run with: java -cp out Tests
public class Tests {
    static int passed = 0;
    static int failed = 0;

    static void check(boolean ok, String name) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    public static void main(String[] args) {
        testDSU();
        testMST();
        testTopological();
        testSCC();
        testDijkstra();
        System.out.println();
        System.out.println("passed: " + passed + ", failed: " + failed);
        if (failed > 0) System.exit(1);
    }

    static void testDSU() {
        DSU d = new DSU(5);
        check(d.find(0) != d.find(1), "DSU: different sets at start");
        check(d.union(0, 1), "DSU: union of two sets returns true");
        check(!d.union(1, 0), "DSU: union in same set returns false");
        d.union(2, 3);
        d.union(1, 3);
        check(d.find(0) == d.find(2), "DSU: 0 and 2 connected after unions");
        check(d.components == 2, "DSU: 2 components left (set {0,1,2,3} and {4})");
    }

    static void testMST() {
        // small graph, MST weight is 1+2+3 = 6
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 2);
        g.addEdge(2, 3, 3);
        g.addEdge(0, 3, 10);
        g.addEdge(0, 2, 5);
        MSTResult k = Kruskal.run(g);
        MSTResult p = Prim.run(g);
        check(k.totalWeight == 6 && p.totalWeight == 6, "MST: small graph weight is 6 for both");
        check(k.connected && p.connected, "MST: small graph is connected");
        check(k.edges.size() == 3 && p.edges.size() == 3, "MST: tree has V-1 edges");

        // single vertex
        Graph one = new Graph(1, false);
        check(Kruskal.run(one).totalWeight == 0 && Prim.run(one).totalWeight == 0, "MST: single vertex weight 0");
        check(Kruskal.run(one).connected && Prim.run(one).connected, "MST: single vertex is connected");

        // disconnected graph: two parts
        Graph dis = new Graph(5, false);
        dis.addEdge(0, 1, 4);
        dis.addEdge(1, 2, 3);
        dis.addEdge(3, 4, 7);
        MSTResult kd = Kruskal.run(dis);
        MSTResult pd = Prim.run(dis);
        check(!kd.connected && !pd.connected, "MST: disconnected graph detected");
        check(kd.components == 2 && pd.components == 2, "MST: 2 components found");
        check(kd.totalWeight == 14 && pd.totalWeight == 14, "MST: forest weight is 14 for both");

        // duplicate weights, many valid MSTs (triangle with equal weights)
        Graph tri = new Graph(3, false);
        tri.addEdge(0, 1, 5);
        tri.addEdge(1, 2, 5);
        tri.addEdge(0, 2, 5);
        check(Kruskal.run(tri).totalWeight == 10 && Prim.run(tri).totalWeight == 10, "MST: equal weights, total 10");

        // graph with cycles and multiple MSTs (square with equal weights)
        Graph sq = new Graph(4, false);
        sq.addEdge(0, 1, 2);
        sq.addEdge(1, 2, 2);
        sq.addEdge(2, 3, 2);
        sq.addEdge(3, 0, 2);
        check(Kruskal.run(sq).totalWeight == 6 && Prim.run(sq).totalWeight == 6, "MST: square with equal weights, total 6");

        // random graphs, Kruskal and Prim must give the same weight
        boolean same = true;
        for (int seed = 0; seed < 30; seed++) {
            Graph r = GraphGenerator.connectedUndirected(50, 200, seed);
            if (Kruskal.run(r).totalWeight != Prim.run(r).totalWeight) same = false;
        }
        check(same, "MST: Kruskal == Prim on 30 random graphs");

        // DSU without optimizations must give same weight too
        Graph r = GraphGenerator.connectedUndirected(100, 500, 7);
        long w1 = Kruskal.run(r).totalWeight;
        long w2 = Kruskal.run(r, new DSU(100, false, false)).totalWeight;
        check(w1 == w2, "MST: Kruskal same weight with and without DSU optimizations");
    }

    static void testTopological() {
        Graph dag = new Graph(6, true);
        dag.addEdge(5, 2, 1);
        dag.addEdge(5, 0, 1);
        dag.addEdge(4, 0, 1);
        dag.addEdge(4, 1, 1);
        dag.addEdge(2, 3, 1);
        dag.addEdge(3, 1, 1);
        TopoResult t = TopologicalSort.run(dag);
        check(!t.hasCycle && t.order.size() == 6, "Topo: DAG has full order");
        check(validOrder(dag, t.order), "Topo: order is valid for small DAG");

        Graph cyc = new Graph(3, true);
        cyc.addEdge(0, 1, 1);
        cyc.addEdge(1, 2, 1);
        cyc.addEdge(2, 0, 1);
        check(TopologicalSort.run(cyc).hasCycle, "Topo: cycle detected");

        Graph one = new Graph(1, true);
        TopoResult t1 = TopologicalSort.run(one);
        check(!t1.hasCycle && t1.order.size() == 1, "Topo: single vertex");

        boolean ok = true;
        for (int seed = 0; seed < 20; seed++) {
            Graph r = GraphGenerator.dag(100, 500, seed);
            TopoResult tr = TopologicalSort.run(r);
            if (tr.hasCycle || !validOrder(r, tr.order)) ok = false;
        }
        check(ok, "Topo: valid order on 20 random DAGs");
    }

    static boolean validOrder(Graph g, List<Integer> order) {
        if (order.size() != g.n) return false;
        int[] pos = new int[g.n];
        for (int i = 0; i < order.size(); i++) {
            pos[order.get(i)] = i;
        }
        for (Edge e : g.edges) {
            if (pos[e.from] >= pos[e.to]) return false;
        }
        return true;
    }

    static void testSCC() {
        // components: {0,1,2}, {3,4}, {5}
        Graph g = new Graph(6, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 4, 1);
        g.addEdge(4, 3, 1);
        g.addEdge(4, 5, 1);
        SCC s = SCC.run(g);
        check(s.components.size() == 3, "SCC: 3 components found");
        check(everyVertexOnce(s, 6), "SCC: every vertex is in exactly one component");

        // DAG: every vertex is its own component
        Graph dag = GraphGenerator.dag(50, 200, 3);
        check(SCC.run(dag).components.size() == 50, "SCC: DAG has V components");

        Graph one = new Graph(1, true);
        check(SCC.run(one).components.size() == 1, "SCC: single vertex");

        boolean ok = true;
        for (int seed = 0; seed < 20; seed++) {
            Graph r = GraphGenerator.directed(100, 300, seed);
            if (!everyVertexOnce(SCC.run(r), 100)) ok = false;
        }
        check(ok, "SCC: every vertex once on 20 random directed graphs");
    }

    static boolean everyVertexOnce(SCC s, int n) {
        int[] count = new int[n];
        for (List<Integer> comp : s.components) {
            for (int v : comp) {
                count[v]++;
            }
        }
        for (int i = 0; i < n; i++) {
            if (count[i] != 1) return false;
        }
        return true;
    }

    static void testDijkstra() {
        Graph g = new Graph(5, false);
        g.addEdge(0, 1, 4);
        g.addEdge(0, 2, 1);
        g.addEdge(2, 1, 2);
        g.addEdge(1, 3, 1);
        // vertex 4 is not connected
        DijkstraResult r = Dijkstra.run(g, 0);
        check(r.dist[1] == 3, "Dijkstra: dist to 1 is 3 (via 2)");
        check(r.dist[3] == 4, "Dijkstra: dist to 3 is 4");
        check(r.dist[4] == DijkstraResult.INF, "Dijkstra: unreachable vertex is INF");
        check(r.pathTo(3).equals(List.of(0, 2, 1, 3)), "Dijkstra: path 0-2-1-3");
        check(r.pathTo(4).isEmpty(), "Dijkstra: no path to unreachable vertex");

        Graph one = new Graph(1, false);
        check(Dijkstra.run(one, 0).dist[0] == 0, "Dijkstra: single vertex");

        // directed graph, one way edge
        Graph d = new Graph(3, true);
        d.addEdge(0, 1, 5);
        d.addEdge(2, 0, 1);
        DijkstraResult rd = Dijkstra.run(d, 0);
        check(rd.dist[1] == 5 && rd.dist[2] == DijkstraResult.INF, "Dijkstra: directed edges respected");

        // compare with simple Bellman-Ford style check on random graph
        Graph rg = GraphGenerator.connectedUndirected(60, 200, 11);
        DijkstraResult rr = Dijkstra.run(rg, 0);
        long[] bf = bellmanFord(rg, 0);
        boolean same = true;
        for (int i = 0; i < rg.n; i++) {
            if (bf[i] != rr.dist[i]) same = false;
        }
        check(same, "Dijkstra: same distances as Bellman-Ford on random graph");

        // negative weight must be rejected
        Graph neg = new Graph(2, true);
        neg.addEdge(0, 1, -3);
        boolean thrown = false;
        try {
            Dijkstra.run(neg, 0);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check(thrown, "Dijkstra: negative weight is rejected");
    }

    // slow but simple, used only to check Dijkstra
    static long[] bellmanFord(Graph g, int s) {
        long[] d = new long[g.n];
        for (int i = 0; i < g.n; i++) {
            d[i] = DijkstraResult.INF;
        }
        d[s] = 0;
        for (int i = 0; i < g.n; i++) {
            for (int v = 0; v < g.n; v++) {
                if (d[v] == DijkstraResult.INF) continue;
                for (Edge e : g.adj.get(v)) {
                    if (d[v] + e.weight < d[e.to]) d[e.to] = d[v] + e.weight;
                }
            }
        }
        return d;
    }
}
