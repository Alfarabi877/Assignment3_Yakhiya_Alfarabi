import java.util.*;

/** Implementations of the six graph algorithms required by Assignment 3. */
public final class Algorithms {
    private Algorithms() {}

    private static final class DSU {
        final int[] parent;
        final int[] size;
        int components;
        int largest;
        DSU(int n) {
            parent = new int[n]; size = new int[n]; components = n; largest = n == 0 ? 0 : 1;
            for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
        }
        int find(int x) {
            int r = x;
            while (parent[r] != r) r = parent[r];
            while (parent[x] != x) { int next = parent[x]; parent[x] = r; x = next; }
            return r;
        }
        boolean union(int a, int b) {
            a = find(a); b = find(b);
            if (a == b) return false;
            if (size[a] < size[b]) { int t = a; a = b; b = t; }
            parent[b] = a; size[a] += size[b]; components--;
            if (size[a] > largest) largest = size[a];
            return true;
        }
    }

    static Map<String, Object> dsu(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] operations = Input.operations(input);
        DSU dsu = new DSU(n);
        List<List<Integer>> states = new ArrayList<>(operations.length);
        for (int[] op : operations) {
            dsu.union(op[0], op[1]);
            states.add(List.of(dsu.components, dsu.largest));
        }
        Map<String, Object> out = new LinkedHashMap<>(); out.put("states", states); return out;
    }

    private static final class Edge {
        final int u, v, weight, id;
        Edge(int u, int v, int weight, int id) { this.u=u; this.v=v; this.weight=weight; this.id=id; }
    }
    private static Edge[] weightedEdges(int[][] rows) {
        Edge[] result = new Edge[rows.length];
        for (int i=0;i<rows.length;i++) result[i] = new Edge(rows[i][0], rows[i][1], rows[i][2], i);
        return result;
    }
    private static Map<String, Object> forestResult(long cost, List<Integer> ids, int components) {
        Map<String, Object> out = new LinkedHashMap<>(); out.put("cost", cost); out.put("edge_ids", ids); out.put("components", components); return out;
    }

    static Map<String, Object> kruskal(Map<String, Object> input) {
        int n = Input.vertexCount(input); Edge[] edges = weightedEdges(Input.edges(input));
        Arrays.sort(edges, Comparator.comparingInt((Edge e) -> e.weight).thenComparingInt(e -> e.id));
        DSU dsu = new DSU(n); long cost = 0; List<Integer> ids = new ArrayList<>();
        for (Edge e : edges) if (dsu.union(e.u, e.v)) { cost += e.weight; ids.add(e.id); }
        return forestResult(cost, ids, dsu.components);
    }

    private static final class AdjEdge {
        final int to, weight, id;
        AdjEdge(int to, int weight, int id) { this.to=to; this.weight=weight; this.id=id; }
    }
    private static final class Candidate {
        final int from, to, weight, id;
        Candidate(int from, int to, int weight, int id) { this.from=from; this.to=to; this.weight=weight; this.id=id; }
    }
    static Map<String, Object> prim(Map<String, Object> input) {
        int n = Input.vertexCount(input); int[][] rows = Input.edges(input);
        List<List<AdjEdge>> graph = new ArrayList<>(n);
        for (int i=0;i<n;i++) graph.add(new ArrayList<>());
        for (int i=0;i<rows.length;i++) {
            int u=rows[i][0], v=rows[i][1], w=rows[i][2];
            graph.get(u).add(new AdjEdge(v,w,i)); graph.get(v).add(new AdjEdge(u,w,i));
        }
        boolean[] visited = new boolean[n];
        PriorityQueue<Candidate> pq = new PriorityQueue<>(Comparator.comparingInt((Candidate c)->c.weight).thenComparingInt(c->c.id));
        List<Integer> ids = new ArrayList<>(); long cost=0; int components=0;
        for (int start=0; start<n; start++) {
            if (visited[start]) continue;
            components++; visited[start]=true;
            for (AdjEdge e: graph.get(start)) if (!visited[e.to]) pq.add(new Candidate(start,e.to,e.weight,e.id));
            while (!pq.isEmpty()) {
                Candidate c=pq.poll();
                if (visited[c.to]) continue;
                visited[c.to]=true; cost += c.weight; ids.add(c.id);
                for (AdjEdge e: graph.get(c.to)) if (!visited[e.to]) pq.add(new Candidate(c.to,e.to,e.weight,e.id));
            }
        }
        return forestResult(cost, ids, components);
    }

    private static List<List<Integer>> adjacency(int n, int[][] edges, boolean reverse) {
        List<List<Integer>> g = new ArrayList<>(n); for(int i=0;i<n;i++) g.add(new ArrayList<>());
        for(int[] e: edges) g.get(reverse ? e[1] : e[0]).add(reverse ? e[0] : e[1]);
        return g;
    }
    /** Iterative DFS finish order; each stack frame stores the next neighbour to visit. */
    private static List<Integer> finishOrder(List<List<Integer>> graph) {
        int n=graph.size(); boolean[] seen=new boolean[n]; List<Integer> order=new ArrayList<>(n);
        int[] vertices=new int[n], nextIndex=new int[n];
        for(int start=0;start<n;start++) {
            if(seen[start]) continue;
            int top=0; vertices[0]=start; nextIndex[0]=0; seen[start]=true;
            while(top>=0) {
                int v=vertices[top];
                if(nextIndex[top] < graph.get(v).size()) {
                    int to=graph.get(v).get(nextIndex[top]++);
                    if(!seen[to]) { seen[to]=true; top++; vertices[top]=to; nextIndex[top]=0; }
                } else { order.add(v); top--; }
            }
        }
        return order;
    }
    private static void collectComponent(List<List<Integer>> graph, int start, int label, int[] component) {
        int[] stack=new int[graph.size()]; int top=0; stack[0]=start; component[start]=label;
        while(top>=0) { int v=stack[top--]; for(int to:graph.get(v)) if(component[to]==-1) { component[to]=label; stack[++top]=to; } }
    }
    static Map<String, Object> scc(Map<String, Object> input) {
        int n=Input.vertexCount(input); int[][] edges=Input.edges(input);
        List<List<Integer>> graph=adjacency(n,edges,false), reverse=adjacency(n,edges,true);
        List<Integer> order=finishOrder(graph); int[] component=new int[n]; Arrays.fill(component,-1); int label=0;
        for(int i=order.size()-1;i>=0;i--) { int v=order.get(i); if(component[v]==-1) collectComponent(reverse,v,label++,component); }
        List<Integer> labels=new ArrayList<>(n); for(int c:component) labels.add(c);
        Set<Long> unique=new HashSet<>(); List<List<Integer>> condensation=new ArrayList<>();
        for(int[] e:edges) if(component[e[0]]!=component[e[1]]) {
            long key=((long)component[e[0]]<<32) | (component[e[1]] & 0xffffffffL);
            if(unique.add(key)) condensation.add(List.of(component[e[0]],component[e[1]]));
        }
        Map<String,Object> out=new LinkedHashMap<>(); out.put("component",labels); out.put("condensation",condensation); return out;
    }

    private static final class DFSResult {
        final boolean acyclic; final List<Integer> order;
        DFSResult(boolean acyclic,List<Integer> order) { this.acyclic=acyclic; this.order=order; }
    }
    /** Three-colour iterative DFS: 0=unvisited, 1=active, 2=finished. */
    private static DFSResult topologicalOrder(List<List<Integer>> graph) {
        int n=graph.size(); byte[] state=new byte[n]; List<Integer> order=new ArrayList<>(n);
        int[] vertices=new int[n], nextIndex=new int[n];
        for(int start=0;start<n;start++) {
            if(state[start]!=0) continue;
            int top=0; vertices[0]=start; nextIndex[0]=0; state[start]=1;
            while(top>=0) {
                int v=vertices[top];
                if(nextIndex[top]<graph.get(v).size()) {
                    int to=graph.get(v).get(nextIndex[top]++);
                    if(state[to]==1) return new DFSResult(false,List.of());
                    if(state[to]==0) { state[to]=1; top++; vertices[top]=to; nextIndex[top]=0; }
                } else { state[v]=2; order.add(v); top--; }
            }
        }
        Collections.reverse(order); return new DFSResult(true,order);
    }
    static Map<String, Object> topo(Map<String, Object> input) {
        int n=Input.vertexCount(input); int[][] edges=Input.edges(input); List<List<Integer>> graph=adjacency(n,edges,false);
        DFSResult result=topologicalOrder(graph); Map<String,Object> out=new LinkedHashMap<>();
        out.put("acyclic",result.acyclic); out.put("order",result.order); return out;
    }

    /** Reconstruct source-to-target vertices from edge IDs. Returns [] if target is unreachable. */
    static List<Integer> reconstructRoute(int source, int target, int[][] edges, int[] parentEdge, boolean[] reachable) {
        if (!reachable[target]) return new ArrayList<>();
        List<Integer> reversed=new ArrayList<>(); int current=target; reversed.add(current);
        while(current!=source) {
            int id=parentEdge[current]; if(id<0) return new ArrayList<>();
            current=edges[id][0]; reversed.add(current);
            if(reversed.size()>parentEdge.length+1) return new ArrayList<>();
        }
        Collections.reverse(reversed); return reversed;
    }
    static Map<String, Object> dag(Map<String, Object> input) {
        int n=Input.vertexCount(input); int source=Input.source(input); int[][] edges=Input.edges(input);
        List<List<Integer>> graph=adjacency(n,edges,false); DFSResult topo=topologicalOrder(graph);
        Map<String,Object> out=new LinkedHashMap<>();
        if(!topo.acyclic) { out.put("acyclic",false); out.put("distance",new ArrayList<>()); out.put("parent_edge",new ArrayList<>()); return out; }
        List<List<Edge>> weighted=new ArrayList<>(n); for(int i=0;i<n;i++) weighted.add(new ArrayList<>());
        for(int i=0;i<edges.length;i++) weighted.get(edges[i][0]).add(new Edge(edges[i][0],edges[i][1],edges[i][2],i));
        long[] dist=new long[n]; boolean[] reachable=new boolean[n]; int[] parent=new int[n]; Arrays.fill(parent,-1); reachable[source]=true;
        for(int v:topo.order) {
            if(!reachable[v]) continue;
            for(Edge e:weighted.get(v)) {
                long candidate=dist[v]+(long)e.weight;
                if(!reachable[e.v] || candidate<dist[e.v]) { reachable[e.v]=true; dist[e.v]=candidate; parent[e.v]=e.id; }
            }
        }
        List<Object> distances=new ArrayList<>(n); List<Integer> parentEdges=new ArrayList<>(n);
        for(int i=0;i<n;i++) { distances.add(reachable[i] ? dist[i] : null); parentEdges.add(parent[i]); }
        // Exercise the required route helper for source, reachable and unreachable targets in tests.
        out.put("acyclic",true); out.put("distance",distances); out.put("parent_edge",parentEdges); return out;
    }
}
