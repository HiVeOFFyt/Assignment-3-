package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;

// Tarjan's algorithm for strongly connected components
public class SCC {
    public List<List<Integer>> components = new ArrayList<>();
    public long edgeExaminations = 0;

    Graph g;
    int[] index;
    int[] low;
    boolean[] onStack;
    int[] stack;
    int top = 0;
    int counter = 0;

    public static SCC run(Graph g) {
        SCC s = new SCC();
        s.g = g;
        s.index = new int[g.n];
        s.low = new int[g.n];
        s.onStack = new boolean[g.n];
        s.stack = new int[g.n];
        for (int i = 0; i < g.n; i++) {
            s.index[i] = -1;
        }
        for (int v = 0; v < g.n; v++) {
            if (s.index[v] == -1) s.dfs(v);
        }
        return s;
    }

    void dfs(int v) {
        index[v] = counter;
        low[v] = counter;
        counter++;
        stack[top++] = v;
        onStack[v] = true;

        for (Edge e : g.adj.get(v)) {
            edgeExaminations++;
            int w = e.to;
            if (index[w] == -1) {
                dfs(w);
                low[v] = Math.min(low[v], low[w]);
            } else if (onStack[w]) {
                low[v] = Math.min(low[v], index[w]);
            }
        }

        // v is the root of a component, pop it from the stack
        if (low[v] == index[v]) {
            List<Integer> comp = new ArrayList<>();
            int w;
            do {
                w = stack[--top];
                onStack[w] = false;
                comp.add(w);
            } while (w != v);
            components.add(comp);
        }
    }
}
