import java.util.*;

/** Validates witnesses independently of the exact answer chosen by an algorithm. */
public final class AssignmentValidationTest {
    private AssignmentValidationTest() {}

    private static Map<String,Object> request(String task, int n, List<List<Integer>> edges) {
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("task", task); r.put("n", n); r.put("edges", edges); return r;
    }
    private static List<List<Integer>> edges(int[][] rows) {
        List<List<Integer>> out = new ArrayList<>();
        for (int[] row : rows) {
            List<Integer> e = new ArrayList<>();
            for (int x : row) e.add(x);
            out.add(e);
        }
        return out;
    }
    private static final class UF {
        int[] p;
        UF(int n) { p = new int[n]; for(int i=0;i<n;i++) p[i]=i; }
        int find(int x) { while(p[x]!=x) { p[x]=p[p[x]]; x=p[x]; } return x; }
        boolean union(int a,int b) { a=find(a); b=find(b); if(a==b)return false; p[b]=a; return true; }
    }
    private static int componentCount(int n, int[][] inputEdges) {
        UF uf=new UF(n); for(int[] e:inputEdges) uf.union(e[0],e[1]);
        Set<Integer> roots=new HashSet<>(); for(int i=0;i<n;i++) roots.add(uf.find(i)); return roots.size();
    }
    private static boolean validForest(int n, int[][] inputEdges, List<Integer> ids, long cost, int components) {
        UF selected=new UF(n); long sum=0; Set<Integer> seen=new HashSet<>();
        for(int id:ids) {
            if(id<0 || id>=inputEdges.length || !seen.add(id)) return false;
            int[] e=inputEdges[id]; if(e[0]==e[1] || !selected.union(e[0],e[1])) return false;
            sum += e[2];
        }
        if(sum!=cost || components!=componentCount(n,inputEdges) || ids.size()!=n-components) return false;
        for(int[] e:inputEdges) if(selected.find(e[0])!=selected.find(e[1])) return false;
        return true;
    }
    private static boolean validTopo(int n, int[][] edges, List<Integer> order) {
        if(order.size()!=n) return false;
        int[] pos=new int[n]; Arrays.fill(pos,-1);
        for(int i=0;i<order.size();i++) { int v=order.get(i); if(v<0||v>=n||pos[v]!=-1)return false; pos[v]=i; }
        for(int[] e:edges) if(pos[e[0]]>=pos[e[1]]) return false;
        return true;
    }
    public static void main(String[] args) {
        int[][] weighted={{0,1,1},{1,2,1},{0,2,1},{2,3,2},{0,3,2},{4,4,-100}};
        Map<String,Object> kr=Algorithms.kruskal(request("kruskal",5,edges(weighted)));
        Map<String,Object> pr=Algorithms.prim(request("prim",5,edges(weighted)));
        long cost=((Number)kr.get("cost")).longValue(); int components=((Number)kr.get("components")).intValue();
        @SuppressWarnings("unchecked") List<Integer> krIds=(List<Integer>)kr.get("edge_ids");
        @SuppressWarnings("unchecked") List<Integer> prIds=(List<Integer>)pr.get("edge_ids");
        if(!validForest(5,weighted,krIds,cost,components)) throw new AssertionError("Kruskal returned invalid forest");
        if(!validForest(5,weighted,prIds,((Number)pr.get("cost")).longValue(),((Number)pr.get("components")).intValue())) throw new AssertionError("Prim returned invalid forest");
        // A different but equally optimal forest must be accepted.
        if(!validForest(5,weighted,List.of(0,2,3),4L,2)) throw new AssertionError("Alternative optimal forest rejected");
        // This witness is invalid: it forms a cycle among 0,1,2 and leaves 3 disconnected.
        if(validForest(5,weighted,List.of(0,1,2),3L,2)) throw new AssertionError("Invalid forest witness accepted");

        int[][] directed={{0,2},{1,2},{2,3}};
        Map<String,Object> topo=Algorithms.topo(request("topo",4,edges(directed)));
        @SuppressWarnings("unchecked") List<Integer> order=(List<Integer>)topo.get("order");
        if(!Boolean.TRUE.equals(topo.get("acyclic")) || !validTopo(4,directed,order)) throw new AssertionError("Topo result invalid");
        if(!validTopo(4,directed,List.of(1,0,2,3))) throw new AssertionError("Alternative valid order rejected");
        if(validTopo(4,directed,List.of(2,0,1,3))) throw new AssertionError("Invalid topological witness accepted");
        System.out.println("AssignmentValidationTest PASS: forest witnesses, alternative optimum, invalid witness, topological witnesses");
    }
}
