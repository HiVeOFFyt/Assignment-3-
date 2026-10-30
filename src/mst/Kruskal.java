package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Kruskal {

    public static MSTResult run(Graph g) {
        return run(g, new DSU(g.n));
    }

    // can give own DSU (for example without optimizations)
    public static MSTResult run(Graph g, DSU dsu) {
        MSTResult res = new MSTResult();
        List<Edge> sorted = new ArrayList<>(g.edges);
        final long[] cmp = {0};
        Collections.sort(sorted, (a, b) -> {
            cmp[0]++;
            return Integer.compare(a.weight, b.weight);
        });
        res.comparisons = cmp[0];

        for (Edge e : sorted) {
            res.edgeExaminations++;
            if (dsu.union(e.from, e.to)) {
                res.edges.add(e);
                res.totalWeight += e.weight;
                if (res.edges.size() == g.n - 1) break;   // tree is ready
            }
        }
        res.components = dsu.components;
        res.connected = (dsu.components == 1);
        res.dsuFinds = dsu.findCalls;
        res.dsuUnions = dsu.unionCalls;
        res.dsuPathSteps = dsu.pathSteps;
        return res;
    }
}
