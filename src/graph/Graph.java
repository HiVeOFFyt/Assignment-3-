package graph;

import java.util.ArrayList;
import java.util.List;

// graph stored as adjacency list
public class Graph {
    public int n;                      // number of vertices
    public boolean directed;
    public List<List<Edge>> adj;       // adj.get(v) = edges going out of v
    public List<Edge> edges;           // every edge once (for Kruskal)

    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        edges = new ArrayList<>();
    }

    public void addEdge(int from, int to, int weight) {
        Edge e = new Edge(from, to, weight);
        edges.add(e);
        adj.get(from).add(e);
        if (!directed) {
            // for undirected graph also add the reverse edge
            adj.get(to).add(new Edge(to, from, weight));
        }
    }
}
