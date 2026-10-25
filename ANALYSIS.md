# Assignment 3 — Graph Algorithms (Java 17)

## Build and run

From the repository root:

```sh
python tools/run.py --setup
python tools/run.py --check
python tools/run.py --input tests/my_cases.jsonl
javac --release 17 -encoding UTF-8 -d .build/Java src/Java/*.java tests/RouteHelperTest.java tests/Benchmark.java
java -ea -cp .build/Java RouteHelperTest
java -ea -cp .build/Java AssignmentValidationTest
java -Xmx1g -ea -cp .build/Java AssignmentStressTest
java -cp .build/Java Benchmark
```

The selected language in `submission.json` is Java. The supplied `Main` reads one JSON request per input line and writes one JSON result per line. All algorithm structures are local to each method call; no graph, visited-state array, DSU or result is retained between requests. This is why a second batch in the same process starts with clean state. Standard output contains only JSON responses in normal program mode.

## 1. DSU (`dsu`)

Each vertex starts as its own set. `parent[v]` points toward the representative of its set, and `size[root]` stores the set size. `find` first finds the root and then compresses the traversed path. `union` finds both roots, does nothing if they match, otherwise attaches the smaller tree below the larger tree. It updates the component count and maximum component size only after a successful merge.

**Invariant:** each DSU tree represents exactly one connected component formed by operations seen so far; `components` is the number of roots and `largest` is the maximum root size. Therefore each recorded state is correct after its operation. Self-unions and repeated unions preserve the partition.

**Complexity:** `O(n + q α(n))` amortized time and `O(n+q)` memory including output states. Boundary cases tested include `n=0`, self-unions, repeated unions and disconnected components.

## 2. Kruskal minimum spanning forest (`kruskal`)

Every edge is stored with its original input index as its ID. Edges are sorted by weight. In ascending order, an edge is accepted only if its endpoints currently belong to different DSU components. This avoids cycles and joins components whenever possible. Self-loops are rejected by DSU naturally. Negative weights, parallel edges and equal weights are valid.

**Invariant:** after each accepted edge, the selected edges form a forest, and the DSU partition matches connectivity in that selected forest. The cut property of a minimum spanning tree justifies selecting the lightest available edge joining two different components. When all edges have been processed, each input connected component has been spanned at minimum cost.

**Complexity:** `O(n + m log(m+1))` time and `O(n+m)` memory. The accumulated cost uses `long` to avoid 32-bit overflow.

## 3. Prim minimum spanning forest (`prim`)

An undirected adjacency list is built with both directions of every edge and the original edge ID. For every not-yet-visited vertex, Prim starts a new component. A min-priority queue holds candidate edges leaving the visited set. The minimum candidate whose destination is still unvisited is accepted; edges leading to already visited vertices are skipped.

**Invariant:** within the current component, accepted edges form a tree and every queued edge crosses from the visited set to an unvisited vertex. Choosing the minimum crossing edge is safe by the cut property. Restarting at every unvisited vertex includes isolated vertices and disconnected components.

**Complexity:** `O(n + m log(m+1))` time with a lazy priority queue and `O(n+m)` memory. Kruskal and Prim were checked to return equal optimum cost and component count on each benchmark graph.

## 4. Strongly connected components (`scc`)

Kosaraju's algorithm is used. The first DFS pass records vertices in finish order in the original graph. The second pass processes that order in reverse on the transposed graph; each traversal assigns one component label. For each original edge, if its endpoints have different labels, the corresponding label pair is added to a hash set before it is appended to the condensation output. This removes duplicate component pairs.

**Invariant:** the first pass orders SCCs by their finishing times; in reverse finishing order, each traversal in the transpose collects exactly one SCC not assigned earlier. Every inter-component edge is then represented in the condensation, while internal edges are excluded.

**Complexity:** `O(n+m)` expected time and `O(n+m)` memory. Labels are arbitrary nonnegative integers; correctness depends on equality of labels, not a specific numbering. Empty graphs, isolated vertices, duplicate edges and self-loops were tested.

## 5. Topological ordering (`topo`)

The DFS uses three states: `0` unvisited, `1` active on the current DFS path, and `2` finished. An edge to an active vertex proves a directed cycle. An edge to a finished vertex does not. If all vertices finish without a back edge, vertices are returned in reverse finish order.

The DFS is iterative rather than recursive. Each stack frame stores the vertex and the index of its next neighbour, so a suspended traversal resumes at the correct place and does not repeatedly scan the same neighbours. This avoids call-stack overflow on deep graphs.

**Invariant:** active vertices are exactly the current DFS path; a transition to an active vertex closes a directed cycle. When acyclic, every edge's destination finishes before its source, so reversing finish order places the source before the destination.

**Complexity:** `O(n+m)` time and memory. For any cycle the output is `acyclic=false` and an empty order. Empty graphs, self-loops and cycles in disconnected components were tested.

## 6. DAG shortest paths (`dag`)

The entire graph is checked for cycles first, not just the source-reachable portion. If a cycle exists anywhere, empty distance and parent arrays are returned. Otherwise, vertices are processed in topological order. Only reachable vertices are relaxed. A candidate distance is the current distance plus edge weight; a parent stores the original ID of the edge that achieved the best distance. Reachability is tracked separately from the numeric distance, so a valid distance of `-1` is not confused with unreachable. JSON `null` represents an unreachable distance.

**Invariant:** when a vertex is processed in topological order, every possible predecessor has already been processed; therefore its shortest distance can be finalized by relaxing incoming possibilities from reachable predecessors. Parent edge IDs point backward along a shortest path, and following them reaches the source.

**Complexity:** `O(n+m)` time and memory. Distances and sums use `long`; unreachable distances are emitted as `null` and their parent IDs are `-1`.

### Route reconstruction helper tests

`Algorithms.reconstructRoute(source, target, edges, parentEdge, reachable)` follows the selected incoming edge IDs backward and reverses the vertex sequence. It returns `[]` for an unreachable target and `[source]` for the source itself. `tests/RouteHelperTest.java` calls the helper directly with assertions enabled.

Test graph edges (original IDs): `0: 0→1 (3)`, `1: 0→2 (8)`, `2: 1→2 (-7)`, `3: 2→3 (4)`, `4: 0→1 (3)` (parallel tied edge). For target `3`, selected IDs `[0,2,3]` produce route `[0,1,2,3]` with total weight `3 + (-7) + 4 = 0`. The route to source `0` is `[0]`; unreachable target `4` returns `[]`. Replacing parent edge `0` with tied parallel edge ID `4` reconstructs the same valid vertex sequence and weight. Each selected edge points from the previous vertex to the next vertex.

## Tests and boundary coverage

`tests/my_cases.jsonl` contains nine mixed requests; `tests/expected_outputs.jsonl` and `tests/actual_outputs.jsonl` preserve expected and observed JSON Lines results for those cases. It covers empty DSU, disconnected weighted graphs, negative weights, parallel edges, duplicate directed edges, SCC condensation, a valid topological order, a disconnected directed cycle, negative DAG distances and a cycle outside source reachability. `tests/RouteHelperTest.java` checks route reconstruction for a reachable target, the source itself, an unreachable target, and a tied parallel edge. `tests/AssignmentValidationTest.java` independently validates minimum-forest witnesses, accepts an alternative optimal forest and topological order, and rejects deliberately invalid witnesses. `tests/AssignmentStressTest.java` exercises `n=100000`, `q=250000`, `m=250000`, and deep SCC/topological/DAG chains. Commands used: `python tools/run.py --setup`, `python tools/run.py --check`, `python tools/run.py --input tests/my_cases.jsonl`, then compile the Java test programs and run `RouteHelperTest`, `AssignmentValidationTest`, and `AssignmentStressTest`. All 36 public cases, state reset, all nine custom JSON cases, both witness tests, route tests, and the full-limit stress test passed in this environment.

## Kruskal vs Prim benchmark

The benchmark harness is `tests/Benchmark.java`. It generates each graph once before timing and passes the same graph to both methods. Each graph consists of a chain plus `2n` seeded random edges, with weights from `-1000` through `1000`. The chain makes the graph connected. The random seed for size `n` is `20261025 + n` (so the seeds are `20262025`, `20266025`, and `20281025`). A warm-up call is made to each method; then each is measured three times with `System.nanoTime()`. Compilation, graph generation, JSON parsing and process startup are excluded. The measured region includes algorithm-internal adjacency construction/sorting, allocation and result construction. Each pair is checked for equal cost and component count before recording timings.

Environment: OpenJDK 21.0.12.1 (the code is compiled with `--release 17`), Python 3.13.5 for the launcher, x86_64 Linux virtual environment, AMD EPYC 9V74 80-Core Processor. Results are in milliseconds; each list is the three measured runs and the median is reported.

| n | m | Kruskal runs (ms) | Kruskal median (ms) | Prim runs (ms) | Prim median (ms) | Minimum cost | Components |
|---:|---:|---|---:|---|---:|---:|---:|
| 1,000 | 3,000 | 3.890, 3.542, 1.607 | 3.542 | 3.613, 3.726, 2.314 | 3.613 | -433,163 | 1 |
| 5,000 | 15,000 | 6.317, 4.884, 6.009 | 6.009 | 6.146, 6.700, 6.671 | 6.671 | -2,226,268 | 1 |
| 20,000 | 60,000 | 40.619, 52.309, 49.923 | 49.923 | 27.950, 28.020, 48.162 | 28.020 | -8,815,004 | 1 |

The timings are noisy: only three measured runs were required, and garbage collection, JIT compilation and virtualized CPU scheduling can influence short measurements. These measurements are not proof that one algorithm is always faster. The theoretical time bounds are both `O(n + m log(m+1))`, but their practical work differs: Kruskal sorts all edges and performs DSU operations; Prim builds adjacency lists and maintains a priority queue of frontier candidates. Larger, repeated experiments on a stable machine would be needed for strong performance conclusions.


## Source and AI acknowledgement

I used OpenAI ChatGPT as a learning aid to help me understand graph algorithms, how the code works, and when and why each algorithm should be used. I am responsible for reviewing the code, understanding its implementation, verifying the results, and disclosing AI assistance in accordance with the course requirements.