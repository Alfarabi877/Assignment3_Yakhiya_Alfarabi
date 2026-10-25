import java.util.Arrays;
import java.util.List;

/** Direct tests of Algorithms.reconstructRoute; run with assertions enabled or normally. */
public final class RouteHelperTest {
    public static void main(String[] args) {
        int[][] edges = {
            {0,1,3}, {0,2,8}, {1,2,-7}, {2,3,4}, {0,1,3}
        };
        // Selected parent edges for shortest routes: 0 -> 1 -> 2 -> 3.
        int[] parent = {-1, 0, 2, 3, -1};
        boolean[] reachable = {true, true, true, true, false};
        assert Algorithms.reconstructRoute(0, 3, edges, parent, reachable).equals(List.of(0,1,2,3));
        assert Algorithms.reconstructRoute(0, 0, edges, parent, reachable).equals(List.of(0));
        assert Algorithms.reconstructRoute(0, 4, edges, parent, reachable).equals(List.of());
        // The parallel edge ID 4 has the same weight as edge 0 and is also a valid tied parent.
        int[] tiedParent = parent.clone(); tiedParent[1] = 4;
        assert Algorithms.reconstructRoute(0, 3, edges, tiedParent, reachable).equals(List.of(0,1,2,3));
        // The selected edge IDs are 0, 2, 3 and their total weight is 0.
        int[] routeEdges = {0, 2, 3}; long total = 0;
        for (int id : routeEdges) total += edges[id][2];
        assert total == 0;
        for (int i = 0; i < routeEdges.length; i++) {
            int id = routeEdges[i];
            if (i + 1 < routeEdges.length) assert edges[id][1] == edges[routeEdges[i + 1]][0];
        }
        System.out.println("RouteHelperTest PASS: reachable, source, unreachable, parallel edges, direction and cost");
    }
}
