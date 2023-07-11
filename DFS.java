package gitlet;

import java.util.ArrayList;
import java.util.Collections;

public class DFS {

    private boolean[] marked;
    private int[] edgeTo;
    private int s;

    public DFS(CommitsGraph graph, int v) {
        this.s = v;
        this.edgeTo = new int[graph.V()];
        this.marked = new boolean[graph.V()];
        depthFirstSearch(graph, v);
    }

    private void depthFirstSearch(CommitsGraph graph, int v) {
        marked[v] = true;
        for (int w : graph.adj(v)) {
            if (!marked[w]) {
                edgeTo[w] = v;
                depthFirstSearch(graph, w);
            }
        }
    }

    public boolean[] getMarked() {
        return marked;
    }

    public ArrayList<Integer> pathTo(int V) {
        ArrayList<Integer> path = new ArrayList<>();
        for (int x = V; x != this.s; x = this.edgeTo[x]) {
            path.add(x);
        }
        path.add(this.s);
        Collections.reverse(path);
        return path;
    }


    public static Integer splitNode(ArrayList<Integer> path, ArrayList<Integer> pathChecked) {
        outer: for (int i = 0; i < path.size(); i++) {
            for (int j = 0; j < pathChecked.size(); j++) {
                if(path.get(i) == pathChecked.get(j)) {
                    return path.get(i);
                }
            }
        }
        return null;
    }
}

