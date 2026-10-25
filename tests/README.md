# Tests

- `my_cases.jsonl`: nine JSON Lines requests covering normal and boundary cases.
- `expected_outputs.jsonl`: expected result lines for those requests.
- `actual_outputs.jsonl`: captured output from `python tools/run.py --input tests/my_cases.jsonl`.
- `RouteHelperTest.java`: directly calls the route reconstruction helper for a reachable target, source, unreachable target and a tied parallel edge. Run with `java -ea -cp .build/Java RouteHelperTest` after compiling it with the supplied Java sources.
- `AssignmentValidationTest.java`: independent witness checks, including an alternative valid optimal forest/order and intentionally invalid witnesses.
- `AssignmentStressTest.java`: deterministic checks at `n=100000`, `q=250000`, `m=250000`, and deep SCC/topological/DAG chains.
- `Benchmark.java`: reproducible in-process Kruskal/Prim benchmark on the same seeded graph at three sizes; details and measured values are in `../ANALYSIS.md`.

Commands from repository root:

```sh
python tools/run.py --setup
python tools/run.py --check
python tools/run.py --input tests/my_cases.jsonl
javac --release 17 -encoding UTF-8 -d .build/Java src/Java/*.java tests/RouteHelperTest.java tests/Benchmark.java tests/AssignmentValidationTest.java tests/AssignmentStressTest.java
java -ea -cp .build/Java RouteHelperTest
java -ea -cp .build/Java AssignmentValidationTest
java -Xmx1g -ea -cp .build/Java AssignmentStressTest
java -cp .build/Java Benchmark
```
