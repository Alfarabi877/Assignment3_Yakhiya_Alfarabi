import java.util.*;

/** Reproducible in-process benchmark. Graph generation is outside the timed region. */
public final class Benchmark {
    private static Map<String,Object> request(String task, int n, int[][] edges) {
        Map<String,Object> r = new HashMap<>(); r.put("task",task); r.put("n",n); List<List<Integer>> rows = new ArrayList<>(); for (int[] e : edges) rows.add(List.of(e[0], e[1], e[2])); r.put("edges",rows); return r;
    }
    private static int[][] makeGraph(int n, long seed) {
        Random random = new Random(seed);
        // A connected chain plus 2n random edges. Chain weights are positive, random weights may be negative.
        int[][] edges = new int[3*n][3];
        for(int i=0;i<n-1;i++) { edges[i][0]=i; edges[i][1]=i+1; edges[i][2]=1+random.nextInt(1000); }
        for(int i=n-1;i<edges.length;i++) {
            int u=random.nextInt(n), v=random.nextInt(n);
            edges[i][0]=u; edges[i][1]=v; edges[i][2]=random.nextInt(2001)-1000;
        }
        return edges;
    }
    private static long time(String task, int n, int[][] edges) {
        Map<String,Object> req=request(task,n,edges);
        long start=System.nanoTime();
        Map<String,Object> result = task.equals("kruskal") ? Algorithms.kruskal(req) : Algorithms.prim(req);
        long elapsed=System.nanoTime()-start;
        if (!result.containsKey("cost") || !result.containsKey("components")) throw new AssertionError("invalid result");
        return elapsed;
    }
    private static long median(long[] a) { Arrays.sort(a); return a[a.length/2]; }
    public static void main(String[] args) {
        int[] sizes = {1000, 5000, 20000};
        System.out.println("seed=20261025; graph=chain plus 2n random edges; weights [-1000,1000]; graph generation excluded; in-process System.nanoTime; 1 warm-up and 3 measured runs; Java="+System.getProperty("java.version"));
        for(int n:sizes) {
            int[][] edges=makeGraph(n,20261025L+n);
            // Warm-up both implementations on the same graph.
            Map<String,Object> k=Algorithms.kruskal(request("kruskal",n,edges));
            Map<String,Object> p=Algorithms.prim(request("prim",n,edges));
            if (!k.get("cost").equals(p.get("cost")) || !k.get("components").equals(p.get("components"))) throw new AssertionError("MST methods disagree");
            long[] kt=new long[3], pt=new long[3];
            for(int i=0;i<3;i++) { kt[i]=time("kruskal",n,edges); pt[i]=time("prim",n,edges); }
            System.out.printf(Locale.ROOT,"n=%d m=%d Kruskal_ms=%s median=%.3f Prim_ms=%s median=%.3f cost=%s components=%s%n",n,edges.length,fmt(kt),median(kt)/1e6,fmt(pt),median(pt)/1e6,k.get("cost"),k.get("components"));
        }
    }
    private static String fmt(long[] a) { StringJoiner s=new StringJoiner(",","[","]"); for(long x:a)s.add(String.format(Locale.ROOT,"%.3f",x/1e6)); return s.toString(); }
}
