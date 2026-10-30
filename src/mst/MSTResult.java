package mst;

import graph.Edge;
import java.util.ArrayList;
import java.util.List;

// result of Kruskal or Prim
public class MSTResult {
    public List<Edge> edges = new ArrayList<>();
    public long totalWeight = 0;
    public boolean connected;        // true if graph is connected (one tree)
    public int components;           // 1 if connected, otherwise we get a forest

    // operation counters
    public long comparisons = 0;     // comparisons (sorting for Kruskal, heap for Prim)
    public long edgeExaminations = 0;
    public long dsuFinds = 0;
    public long dsuUnions = 0;
    public long dsuPathSteps = 0;
    public long heapOps = 0;
}
