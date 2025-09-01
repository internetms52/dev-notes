# Reference-Chain Lock Pattern
This pattern utilizes reference chaining between concurrent threads, where multiple threads share
the same ReentrantLock object through references. When the last thread releases its reference,
the lock object becomes eligible for garbage collection automatically, eliminating the need for
explicit cleanup scheduling or memory management.

## Explain
```mermaid
sequenceDiagram
    participant ThreadA
    participant Map
    participant Lock_I as Lock "I"
    participant ThreadB

    Note over ThreadA, ThreadB: Concurrent execution scenario

    ThreadA->>Map: Check for lock "I"
    Map-->>ThreadA: Not found
    ThreadA->>Lock_I: Create new lock "I"
    ThreadA->>Map: Put lock "I" in map
    
    par ThreadA continues working
        ThreadA->>Lock_I: Acquire lock
        ThreadA->>Lock_I: Perform work
    and ThreadB starts
        ThreadB->>Map: Check for lock "I"
        Map-->>ThreadB: Found existing lock
        ThreadB->>Lock_I: Get reference to lock "I"
        ThreadB->>Lock_I: Wait for lock (blocked)
    end
    
    ThreadA->>Lock_I: Release lock
    ThreadA->>Map: Remove entry from map
    Note over ThreadA: ThreadA finished
    
    ThreadB->>Lock_I: Acquire lock (unblocked)
    ThreadB->>Lock_I: Perform work
    ThreadB->>Lock_I: Release lock
    ThreadB->>Map: Try to remove entry (already removed)
    Note over Lock_I: No more references
    Note over Lock_I: Eligible for GC
```