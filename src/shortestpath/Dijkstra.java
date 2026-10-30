package shortestpath;

import graph.Edge;
import graph.Graph;
import java.util.Arrays;
import java.util.PriorityQueue;

public class Dijkstra {

    public static DijkstraResult run(Graph g, int source) {
        DijkstraResult res = new DijkstraResult();
        res.dist = new long[g.n];
        res.prev = new int[g.n];
        Arrays.fill(res.dist, DijkstraResult.INF);
        Arrays.fill(res.prev, -1);
        res.dist[source] = 0;

        // queue holds {vertex, distance}
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[1], b[1]));
        pq.add(new long[]{source, 0});
        res.heapOps++;

        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            res.heapOps++;
            int v = (int) cur[0];
            if (cur[1] > res.dist[v]) continue;     // old entry, skip

            for (Edge e : g.adj.get(v)) {
                if (e.weight < 0) {
                    throw new IllegalArgumentException("Dijkstra does not work with negative weights");
                }
                res.edgeRelaxations++;
                long nd = res.dist[v] + e.weight;
                if (nd < res.dist[e.to]) {
                    res.dist[e.to] = nd;
                    res.prev[e.to] = v;
                    res.successfulRelaxations++;
                    pq.add(new long[]{e.to, nd});
                    res.heapOps++;
                }
            }
        }
        return res;
    }
}
