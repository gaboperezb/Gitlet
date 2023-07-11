package gitlet;

import edu.princeton.cs.algs4.Graph;

import java.util.ArrayList;
import java.util.List;

public class CommitsGraph {

    private final int V;
    private List<Integer>[] adj;

    public CommitsGraph(int V) {
        this.V = V;
        adj = (List<Integer>[]) new ArrayList[V];
        for (int v = 0; v < V; v++) {
            adj[v] = new ArrayList<Integer>();
        }
    }

    public int V() {
        return this.V;
    }

    public void addEdge(int v, int w) {
        adj[v].add(w);
    }

    public Iterable<Integer> adj(int v) {
        return adj[v];
    }


    public List<Integer>[] getEdges() {
        return this.adj;
    }

}
