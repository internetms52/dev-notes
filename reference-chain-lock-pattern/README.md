# Reference-Chain Lock Pattern
This pattern utilizes reference chaining between concurrent threads, where multiple threads share
the same ReentrantLock object through references. When the last thread releases its reference,
the lock object becomes eligible for garbage collection automatically, eliminating the need for
explicit cleanup scheduling or memory management.

## Explain
```mermaid
flowchart TD
    Start([Start]) --> Check{Check if lock "I"<br/>exists in map}

    Check -->|Not exists| Create[A: Create new lock "I"<br/>and add to map]
    Check -->|Exists| Reference[B: Get reference to<br/>existing lock "I"]
    
    Create --> AWork[A: Use lock to<br/>perform work]
    Reference --> BWork[B: Use lock to<br/>perform work]
    
    AWork --> AFinish[A: Work finished<br/>Remove entry from map]
    BWork --> BFinish[B: Work finished<br/>Try to remove entry from map]
    
    AFinish --> BStillWork[B: Still holds lock reference<br/>Continue working]
    BFinish --> BRelease[B: Release lock reference]
    
    BStillWork --> BRelease
    BRelease --> GC[No references to lock<br/>Automatically GC collected]
    
    GC --> End([End])
```