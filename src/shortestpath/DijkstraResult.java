package shortestpath;

import java.util.ArrayList;
import java.util.List;

public class DijkstraResult {
    public static final long INF = Long.MAX_VALUE;
    public long[] dist;
    public int[] prev;
    public long edgeRelaxations = 0;   // edge examinations
    public long heapOps = 0;
    public long successfulRelaxations = 0;

    // path from source to target, empty list if unreachable
    public List<Integer> pathTo(int target) {
        List<Integer> path = new ArrayList<>();
        if (dist[target] == INF) return path;
        for (int v = target; v != -1; v = prev[v]) {
            path.add(0, v);
        }
        return path;
    }
}
