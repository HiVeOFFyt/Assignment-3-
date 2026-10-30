package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.Queue;

// Kahn's algorithm (BFS with in-degrees)
public class TopologicalSort {

    public static TopoResult run(Graph g) {
        TopoResult res = new TopoResult();
        int[] indeg = new int[g.n];
        for (Edge e : g.edges) {
            indeg[e.to]++;
            res.edgeExaminations++;
        }
        Queue<Integer> q = new ArrayDeque<>();
        for (int v = 0; v < g.n; v++) {
            if (indeg[v] == 0) q.add(v);
        }
        while (!q.isEmpty()) {
            int v = q.poll();
            res.order.add(v);
            for (Edge e : g.adj.get(v)) {
                res.edgeExaminations++;
                indeg[e.to]--;
                if (indeg[e.to] == 0) q.add(e.to);
            }
        }
        // if not all vertices are in the order, there is a cycle
        if (res.order.size() < g.n) {
            res.hasCycle = true;
            res.order.clear();
        }
        return res;
    }
}
