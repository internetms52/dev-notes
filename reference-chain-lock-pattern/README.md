# Reference-Chain Lock Pattern
This pattern utilizes reference chaining between concurrent threads, where multiple threads share
the same ReentrantLock object through references. When the last thread releases its reference,
the lock object becomes eligible for garbage collection automatically, eliminating the need for
explicit cleanup scheduling or memory management.

## Explain
```mermaid
  root(chained-scenario)-->A["A(create lock "I")"]
  A-->B["B(reference lock "I")"]
  B-->C["A(finished, remove map entry.)"]
  C-->D["B(keep the reference.)"]
  D-->E["B(finished, remove map entry.)"]
  E-->F["lock "I" null reference.]
```