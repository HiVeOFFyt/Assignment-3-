import connectivity.SCC;
import connectivity.TopoResult;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

// runs all experiments and writes csv tables to results/tables
public class Main {
    static final long SEED = 42;
    static final int RUNS = 5;
    static final int WARMUP = 100;   // untimed runs before measuring (JIT)
    static final String[] NAMES = {"Small", "Medium", "Large"};
    static final int[] V = {10, 100, 1000};
    static final int[] E = {30, 500, 5000};
    static final String DIR = "results/tables/";

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        globalWarmup();
        mstExperiment();
        dsuExperiment();
        topoExperiment();
        sccExperiment();
        dijkstraExperiment();
        System.out.println("Done. Tables are in " + DIR);
    }

    // run every algorithm many times on every size, so the JVM compiles the code
    // before we start measuring (results are thrown away)
    static void globalWarmup() {
        for (int round = 0; round < 300; round++) {
            for (int i = 0; i < 3; i++) {
                Graph u = GraphGenerator.connectedUndirected(V[i], E[i], round);
                Graph d = GraphGenerator.dag(V[i], E[i], round);
                Graph c = GraphGenerator.directed(V[i], E[i], round);
                Kruskal.run(u);
                Prim.run(u);
                Kruskal.run(u, new DSU(u.n, false, false));
                TopologicalSort.run(d);
                TopologicalSort.run(c);
                SCC.run(c);
                Dijkstra.run(u, 0);
            }
        }
    }

    // average time in microseconds
    static double us(long totalNanos) {
        return totalNanos / (double) RUNS / 1000.0;
    }

    static void mstExperiment() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(DIR + "mst.csv"));
        out.println("graph,V,E,algorithm,avg_time_us,total_weight,comparisons,edge_examinations,dsu_finds,dsu_unions,heap_ops");
        for (int i = 0; i < 3; i++) {
            Graph g = GraphGenerator.connectedUndirected(V[i], E[i], SEED);
            // warm up, so JIT does not ruin the first measurement
            for (int w = 0; w < WARMUP; w++) {
                Kruskal.run(g);
                Prim.run(g);
            }

            long tk = 0;
            long tp = 0;
            MSTResult k = null;
            MSTResult p = null;
            for (int r = 0; r < RUNS; r++) {
                long s = System.nanoTime();
                k = Kruskal.run(g);
                tk += System.nanoTime() - s;
                s = System.nanoTime();
                p = Prim.run(g);
                tp += System.nanoTime() - s;
            }
            out.printf("%s,%d,%d,Kruskal,%.2f,%d,%d,%d,%d,%d,0%n", NAMES[i], V[i], E[i], us(tk),
                    k.totalWeight, k.comparisons, k.edgeExaminations, k.dsuFinds, k.dsuUnions);
            out.printf("%s,%d,%d,Prim,%.2f,%d,%d,%d,0,0,%d%n", NAMES[i], V[i], E[i], us(tp),
                    p.totalWeight, p.comparisons, p.edgeExaminations, p.heapOps);
        }
        out.close();
    }

    // Kruskal with 4 kinds of DSU
    static void dsuExperiment() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(DIR + "dsu.csv"));
        out.println("graph,V,E,variant,avg_time_us,find_calls,union_calls,parent_steps");
        boolean[][] opt = {{false, false}, {true, false}, {false, true}, {true, true}};
        String[] names = {"no optimization", "path compression only", "union by rank only", "both"};
        for (int i = 0; i < 3; i++) {
            Graph g = GraphGenerator.connectedUndirected(V[i], E[i], SEED);
            for (int v = 0; v < 4; v++) {
                for (int w = 0; w < WARMUP; w++) {
                    Kruskal.run(g, new DSU(g.n, opt[v][0], opt[v][1]));   // warm up
                }
                long t = 0;
                MSTResult res = null;
                for (int r = 0; r < RUNS; r++) {
                    DSU d = new DSU(g.n, opt[v][0], opt[v][1]);
                    long s = System.nanoTime();
                    res = Kruskal.run(g, d);
                    t += System.nanoTime() - s;
                }
                out.printf("%s,%d,%d,%s,%.2f,%d,%d,%d%n", NAMES[i], V[i], E[i], names[v], us(t),
                        res.dsuFinds, res.dsuUnions, res.dsuPathSteps);
            }
        }
        out.close();
    }

    static void topoExperiment() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(DIR + "topo.csv"));
        out.println("graph,V,E,input,avg_time_us,cycle_found,order_size,edge_examinations");
        for (int i = 0; i < 3; i++) {
            Graph dag = GraphGenerator.dag(V[i], E[i], SEED);
            Graph cyc = GraphGenerator.directed(V[i], E[i], SEED);
            Graph[] gs = {dag, cyc};
            String[] types = {"DAG", "with cycles"};
            for (int j = 0; j < 2; j++) {
                for (int w = 0; w < WARMUP; w++) {
                    TopologicalSort.run(gs[j]);   // warm up
                }
                long t = 0;
                TopoResult res = null;
                for (int r = 0; r < RUNS; r++) {
                    long s = System.nanoTime();
                    res = TopologicalSort.run(gs[j]);
                    t += System.nanoTime() - s;
                }
                out.printf("%s,%d,%d,%s,%.2f,%b,%d,%d%n", NAMES[i], V[i], E[i], types[j], us(t),
                        res.hasCycle, res.order.size(), res.edgeExaminations);
            }
        }
        out.close();
    }

    static void sccExperiment() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(DIR + "scc.csv"));
        out.println("graph,V,E,avg_time_us,num_components,largest_component,edge_examinations");
        for (int i = 0; i < 3; i++) {
            Graph g = GraphGenerator.directed(V[i], E[i], SEED);
            for (int w = 0; w < WARMUP; w++) {
                SCC.run(g);   // warm up
            }
            long t = 0;
            SCC res = null;
            for (int r = 0; r < RUNS; r++) {
                long s = System.nanoTime();
                res = SCC.run(g);
                t += System.nanoTime() - s;
            }
            int biggest = 0;
            for (java.util.List<Integer> c : res.components) {
                biggest = Math.max(biggest, c.size());
            }
            out.printf("%s,%d,%d,%.2f,%d,%d,%d%n", NAMES[i], V[i], E[i], us(t),
                    res.components.size(), biggest, res.edgeExaminations);
        }
        out.close();
    }

    static void dijkstraExperiment() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(DIR + "dijkstra.csv"));
        out.println("graph,V,E,avg_time_us,reachable,edge_relaxations,successful_relaxations,heap_ops");
        for (int i = 0; i < 3; i++) {
            Graph g = GraphGenerator.connectedUndirected(V[i], E[i], SEED);
            for (int w = 0; w < WARMUP; w++) {
                Dijkstra.run(g, 0);   // warm up
            }
            long t = 0;
            DijkstraResult res = null;
            for (int r = 0; r < RUNS; r++) {
                long s = System.nanoTime();
                res = Dijkstra.run(g, 0);
                t += System.nanoTime() - s;
            }
            int reachable = 0;
            for (long d : res.dist) {
                if (d != DijkstraResult.INF) reachable++;
            }
            out.printf("%s,%d,%d,%.2f,%d,%d,%d,%d%n", NAMES[i], V[i], E[i], us(t),
                    reachable, res.edgeRelaxations, res.successfulRelaxations, res.heapOps);
        }
        out.close();
    }
}
