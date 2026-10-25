# Assignment 3 — Graph Algorithms (Java 17)

## Open locally in IntelliJ IDEA
1. Open this folder with **File → Open**.
2. Set Project SDK to JDK 17 and import Maven if prompted.
3. The required source files are under `src/Java/`.

## Required course checks
Run these commands from the project root:

```bash
python tools/run.py --setup
python tools/run.py --check
```

To run the included JSON Lines examples:

```bash
python tools/run.py --input tests/my_cases.jsonl
```

## Additional Java checks
Compile the source and included test/benchmark helpers with Java 17:

```bash
mkdir -p .build/local
javac --release 17 -encoding UTF-8 -d .build/local src/Java/Algorithms.java src/Java/Input.java src/Java/Json.java src/Java/Main.java tests/RouteHelperTest.java tests/Benchmark.java tests/AssignmentValidationTest.java tests/AssignmentStressTest.java
java -ea -cp .build/local RouteHelperTest
java -ea -cp .build/local AssignmentValidationTest
java -Xmx1g -ea -cp .build/local AssignmentStressTest
java -cp .build/local Benchmark
```

Review `ANALYSIS.md`, especially the benchmark environment and timings. Follow the course rules for acknowledging AI assistance. This folder is a local project; it is not uploaded to GitHub or Moodle automatically.
