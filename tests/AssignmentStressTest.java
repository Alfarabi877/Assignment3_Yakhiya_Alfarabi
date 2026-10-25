import java.util.*;

/** Deterministic large-input tests for the assignment limits and deep iterative DFS. */
public final class AssignmentStressTest {
    private AssignmentStressTest() {}
    private static Map<String,Object> request(String task, int n, Object fieldValue, String fieldName) {
        Map<String,Object> r = new LinkedHashMap<>(); r.put("task",task); r.put("n",n); r.put(fieldName,fieldValue); return r;
    }
    private static List<List<Integer>> pairs(int count, int n) {
        List<List<Integer>> rows = new ArrayList<>(count);
        for(int i=0;i<count;i++) rows.add(List.of(i % n, (int)(((long)i*31+1)%n)));
        return rows;
    }
    private static List<List<Integer>> chain(int n) {
        List<List<Integer>> rows = new ArrayList<>(n-1);
        for(int i=0;i<n-1;i++) rows.add(List.of(i,i+1));
        return rows;
    }
    private static List<List<Integer>> weightedGraph(int n, int m) {
        List<List<Integer>> rows = new ArrayList<>(m);
        for(int i=0;i<n-1;i++) rows.add(List.of(i,i+1,1));
        Random random = new Random(20261025L);
        while(rows.size()<m) {
            int u=random.nextInt(n), v=random.nextInt(n);
            rows.add(List.of(u,v,1000+random.nextInt(1001)));
        }
        return rows;
    }
    public static void main(String[] args) {
        final int n=100_000;
        Map<String,Object> dsuReq=request("dsu",n,pairs(250_000,n),"operations");
        @SuppressWarnings("unchecked") List<?> states=(List<?>)Algorithms.dsu(dsuReq).get("states");
        if(states.size()!=250_000) throw new AssertionError("DSU full operation limit failed");

        List<List<Integer>> chain=chain(n);
        Map<String,Object> scc=Algorithms.scc(request("scc",n,chain,"edges"));
        @SuppressWarnings("unchecked") List<?> labels=(List<?>)scc.get("component");
        @SuppressWarnings("unchecked") List<?> condensation=(List<?>)scc.get("condensation");
        if(labels.size()!=n || condensation.size()!=n-1) throw new AssertionError("SCC deep chain failed");
        Map<String,Object> topo=Algorithms.topo(request("topo",n,chain,"edges"));
        @SuppressWarnings("unchecked") List<?> order=(List<?>)topo.get("order");
        if(!Boolean.TRUE.equals(topo.get("acyclic")) || order.size()!=n || !order.get(0).equals(0) || !order.get(n-1).equals(n-1)) throw new AssertionError("Topo deep chain failed");

        List<List<Integer>> dagEdges=new ArrayList<>(n-1);
        for(int i=0;i<n-1;i++) dagEdges.add(List.of(i,i+1,-1));
        Map<String,Object> dagReq=request("dag",n,dagEdges,"edges"); dagReq.put("source",0);
        Map<String,Object> dag=Algorithms.dag(dagReq);
        @SuppressWarnings("unchecked") List<Object> distances=(List<Object>)dag.get("distance");
        if(!Boolean.TRUE.equals(dag.get("acyclic")) || !Long.valueOf(-(n-1L)).equals(distances.get(n-1))) throw new AssertionError("DAG deep chain failed");

        List<List<Integer>> weighted=weightedGraph(n,250_000);
        Map<String,Object> krReq=request("kruskal",n,weighted,"edges");
        Map<String,Object> prReq=request("prim",n,weighted,"edges");
        Map<String,Object> kr=Algorithms.kruskal(krReq), pr=Algorithms.prim(prReq);
        if(!kr.get("cost").equals(pr.get("cost")) || !kr.get("components").equals(pr.get("components"))) throw new AssertionError("MST full edge limit failed");
        if(!Long.valueOf(n-1L).equals(kr.get("cost")) || !Integer.valueOf(1).equals(kr.get("components"))) throw new AssertionError("Unexpected full-limit MST result");
        System.out.println("AssignmentStressTest PASS: n=100000, q=250000, m=250000, deep SCC/topo/DAG chains");
    }
}
