package mst;

import graph.Edge;
import graph.Graph;
import java.util.PriorityQueue;

public class Prim {

    public static MSTResult run(Graph g) {
        MSTResult res = new MSTResult();
        final long[] cmp = {0};
        boolean[] visited = new boolean[g.n];
        int comps = 0;

        // start from every unvisited vertex, so disconnected graph gives a forest
        for (int start = 0; start < g.n; start++) {
            if (visited[start]) continue;
            comps++;
            PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {
                cmp[0]++;
                return Integer.compare(a.weight, b.weight);
            });
            visited[start] = true;
            for (Edge e : g.adj.get(start)) {
                res.edgeExaminations++;
                pq.add(e);
                res.heapOps++;
            }
            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                res.heapOps++;
                if (visited[e.to]) continue;       // would make a cycle
                visited[e.to] = true;
                res.edges.add(e);
                res.totalWeight += e.weight;
                for (Edge next : g.adj.get(e.to)) {
                    res.edgeExaminations++;
                    if (!visited[next.to]) {
                        pq.add(next);
                        res.heapOps++;
                    }
                }
            }
        }
        res.comparisons = cmp[0];
        res.components = comps;
        res.connected = (comps == 1);
        return res;
    }
}
