# AI Assignment 1

8-Puzzle search algorithms and CSP-based bot scheduling, written in Java.

## Project Structure

```
question1/
  inputfile/input.txt
  PuzzleState.java          -- shared utilities, heuristics, I/O
  BFSSearch.java             -- breadth-first search
  DFSSearch.java             -- depth-first search (depth limit = 50)
  AStarSearch.java           -- A* with h1 (misplaced tiles) and h2 (manhattan)
  IDAStarSearch.java         -- iterative deepening A*
  GreedyBestFirstSearch.java -- greedy best-first with h2
  SimulatedAnnealingSearch.java -- simulated annealing
  AdversarialSearch.java     -- minimax + alpha-beta pruning

question2/
  inputfile/input.txt
  CSPFramework.java          -- shared constraints, domains, I/O
  BacktrackingSearch.java    -- backtracking + MRV + forward checking
  ForwardCheckingDemo.java   -- step-by-step forward checking walkthrough
  ArcConsistency.java        -- AC-3 preprocessing + backtracking
```
## Setup (GitHub Codespaces)

Open the repo in a Codespace — Java 11+ comes preinstalled. Verify with:

```bash
java -version
```

## Question 1 — 8-Puzzle

Search algorithms for solving the manuscript sorting (8-puzzle) problem.

**Input file:** `question1/inputfile/input.txt`

Every two lines make one puzzle. First line is start state, second is goal state. `B` is the blank tile.

```
123;B46;758
123 456 78B
```

**Compile and run:**

```bash
cd question1
javac *.java

java BFSSearch
java DFSSearch
java AStarSearch
java IDAStarSearch
java GreedyBestFirstSearch
java SimulatedAnnealingSearch
java AdversarialSearch
```

To use a different input file:

```bash
java BFSSearch path/to/input.txt
```

## Question 2 — Bot Scheduling (CSP)

Constraint satisfaction for scheduling security bots across time slots.

**Input file:** `question2/inputfile/input.txt`

```
BOTS: A, B, C
SLOTS: 1, 2, 3, 4
BOT_C_NOT_IN: 4
```

Constraints: no bot works back-to-back slots, Bot C skips Slot 4, every bot gets at least one slot.

**Compile and run:**

```bash
cd question2
javac *.java

java BacktrackingSearch
java ForwardCheckingDemo
java ArcConsistency
```

To use a different input file:

```bash
java BacktrackingSearch path/to/input.txt
```


