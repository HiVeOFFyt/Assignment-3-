package dsu;

// Disjoint Set Union
// path compression and union by rank can be switched on/off to compare them
public class DSU {
    public int[] parent;
    public int[] rank;
    public int components;
    boolean useCompression;
    boolean useRank;

    // counters
    public long findCalls = 0;
    public long unionCalls = 0;
    public long pathSteps = 0;     // how many parent links we followed

    public DSU(int n) {
        this(n, true, true);
    }

    public DSU(int n, boolean useCompression, boolean useRank) {
        this.useCompression = useCompression;
        this.useRank = useRank;
        parent = new int[n];
        rank = new int[n];
        components = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
    }

    public int find(int x) {
        findCalls++;
        int root = x;
        while (parent[root] != root) {
            root = parent[root];
            pathSteps++;
        }
        if (useCompression) {
            // second pass: point everything on the path to the root
            while (parent[x] != root) {
                int next = parent[x];
                parent[x] = root;
                x = next;
            }
        }
        return root;
    }

    // returns false if a and b were already in the same set
    public boolean union(int a, int b) {
        unionCalls++;
        int ra = find(a);
        int rb = find(b);
        if (ra == rb) return false;
        if (useRank) {
            if (rank[ra] < rank[rb]) {
                parent[ra] = rb;
            } else if (rank[ra] > rank[rb]) {
                parent[rb] = ra;
            } else {
                parent[rb] = ra;
                rank[ra]++;
            }
        } else {
            parent[rb] = ra;
        }
        components--;
        return true;
    }
}
