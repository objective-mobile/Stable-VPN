# Interface Implementation Rules

## Core Implementation Principle

When creating implementations for existing interfaces, follow these strict guidelines to maintain
clean architecture and separation of concerns.

## Implementation Rule

**If I give a command that needs to create realization for interface:**

- **DO NOT** make changes to existing interfaces
- **DO NOT** modify existing domain models or data models
- **DO** create Base implementations in the appropriate layer:
    - Most implementations should go in the `:data` layer as `BaseRepository` classes
    - UI-related implementations should go in the `:presentation` layer
- **DO** implement only the methods that already exist in the interface
- **DO** follow the existing naming conventions (e.g., `CountryBaseRepository` for
  `CountryRepository`)

## Examples

### Correct Approach

```kotlin
// Existing interface (DO NOT MODIFY)
interface VpnRepository {
    suspend fun connect(): ConnectionResult
    suspend fun disconnect(): DisconnectionResult
    suspend fun status(): ConnectionStatus
}

// Create implementation in :data layer
class VpnBaseRepository(
    private val vpnService: VpnService
) : VpnRepository {
    override suspend fun connect(): ConnectionResult {
        // Implementation here
    }
    
    override suspend fun disconnect(): DisconnectionResult {
        // Implementation here
    }
    
    override suspend fun status(): ConnectionStatus {
        // Implementation here
    }
}
```

### Incorrect Approach

```kotlin
// ❌ DO NOT modify existing interfaces
interface VpnRepository {
    suspend fun connect(): ConnectionResult
    suspend fun disconnect(): DisconnectionResult
    suspend fun status(): ConnectionStatus
    suspend fun newMethod(): String // ❌ Don't add new methods
}

// ❌ DO NOT modify existing models
data class ConnectionResult(
    val success: Boolean,
    val newField: String // ❌ Don't add new fields
)
```

## Layer-Specific Guidelines

### Data Layer Implementations

- Use `BaseRepository` suffix for repository implementations
- Place in `{module}/data/repository/` package
- Implement data access logic and external service integration

### Presentation Layer Implementations

- Use appropriate naming for UI-related implementations
- Place in `app/presentation/` submodules
- Handle UI state management and user interactions

## Code Comments Rule

**Don't add comments in code. Add comments only if it's described in requirements.**

- Code should be self-explanatory through proper naming and structure
- Comments are only allowed when explicitly specified in project requirements
- Focus on writing clean, readable code that doesn't need explanatory comments
- Use meaningful variable names, function names, and class names instead of comments

## Compliance

This rule ensures:

- **Interface stability** - Existing contracts remain unchanged
- **Backward compatibility** - No breaking changes to existing code
- **Clean separation** - Implementation details stay in appropriate layers
- **Maintainability** - Clear distinction between contracts and implementations
- **Clean code** - Self-documenting code without unnecessary comments