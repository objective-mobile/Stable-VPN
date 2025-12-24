# StableVPN Project Architecture Rules

## Overview

This document defines the architectural principles and coding standards for the StableVPN Android
application. The project follows Clean Architecture principles with a modular structure designed for
maintainability, testability, and separation of concerns.

## Module Structure

The project is organized into four main modules, each with specific responsibilities:

### 1. `:app` Module

- **Purpose**: Application entry point and presentation layer
- **Responsibility**: Collects all functionality and serves as the starting point
- **Contains**:
    - `:presentation` submodule (UI components, screens, ViewModels)
    - Main Activity and application-level configuration
- **Dependencies**: Can depend on all other modules' `:domain` submodules and `:data` submodules

### 2. `:countries` Module

- **Purpose**: Country data management
- **Responsibility**: Handles country information, flags, and related data
- **Submodules**: `:data`, `:domain`
- **Dependencies**: Self-contained, no external module dependencies

### 3. `:permissions` Module

- **Purpose**: Android permissions handling
- **Responsibility**: Manages all permission-related logic and requests
- **Submodules**: `:data`, `:domain`
- **Dependencies**: Self-contained, no external module dependencies

### 4. `:vpn` Module

- **Purpose**: VPN functionality
- **Responsibility**: Low-level VPN operations, connection management
- **Submodules**: `:data`, `:domain`
- **Dependencies**: Self-contained, no external module dependencies

## Submodule Architecture

Each module follows a consistent internal structure:

### `:domain` Submodule

- **Contains**: Business logic, domain objects, domain models, repository interfaces
- **Responsibility**: Core business rules and entities with rich behavior
- **Dependencies**: No dependencies on other layers (pure Kotlin/Java)
- **Accessibility**: All modules can depend on any `:domain` submodule

### `:data` Submodule

- **Contains**: Repository implementations, data sources, network/database logic
- **Responsibility**: Data access and external service integration
- **Dependencies**: Can depend on corresponding `:domain` submodule
- **Accessibility**: Only `:app` module can depend on `:data` submodules

## Dependency Rules

### Dependency Inversion Principle

- Higher-level modules should not depend on lower-level modules
- Both should depend on abstractions (interfaces)
- Abstractions should not depend on details; details should depend on abstractions

### Module Dependency Matrix

| Module                | Can Depend On                        |
|-----------------------|--------------------------------------|
| `:app`                | All `:domain` and `:data` submodules |
| `:countries:domain`   | None (pure business logic)           |
| `:countries:data`     | `:countries:domain` only             |
| `:permissions:domain` | None (pure business logic)           |
| `:permissions:data`   | `:permissions:domain` only           |
| `:vpn:domain`         | None (pure business logic)           |
| `:vpn:data`           | `:vpn:domain` only                   |

### Forbidden Dependencies

- `:data` submodules cannot depend on other modules' `:data` submodules
- `:domain` submodules cannot depend on `:data` submodules
- Feature modules (`:countries`, `:permissions`, `:vpn`) cannot depend on each other directly

## Dependency Injection Strategy

### No DI Frameworks

- The project deliberately avoids dependency injection frameworks (Dagger, Hilt, Koin)
- Dependencies are provided through custom ViewModel factories
- Manual dependency construction promotes explicit dependency management

### ViewModel Factory Pattern

```kotlin
// Example: Custom ViewModel factory
class VpnViewModelFactory(
    private val vpnRepository: VpnRepository,
    private val permissionsRepository: PermissionsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VpnViewModel(vpnRepository, permissionsRepository) as T
    }
}
```

## Elegant Objects Naming Rules

### Core Principles

#### 1. Objects Are Nouns Only

- ❌ `calculateSalary()`
- ✅ `Salary`
- Objects represent things, not actions. An object knows how to do something — but its name must be
  a thing, not a verb.

#### 2. No Getters/Setters

- ❌ `getName()` ❌ `setAge()`
- ✅ `name()` ✅ `age()`
- Calling a method is asking an object to do work, not extracting data.
- Examples: `user.name()`, `order.total()`
- No get, set, fetch, load, save prefixes.

#### 3. No "Manager", "Helper", "Util", "Service", "Processor"

- These are anti-names — they mean "I didn't design this."
- ❌ `PaymentManager` ❌ `StringUtil` ❌ `UserService`
- They hide responsibility and create god-objects.
- Instead: ✅ `Payments` ✅ `EncryptedText` ✅ `Users`

#### 4. Avoid "Abstract", "Base", "Common"

- If the name contains inheritance mechanics — your design is broken.
- ❌ `BaseController` ❌ `AbstractParser`
- Objects are final concepts, not framework nodes.

#### 5. No Technical Suffixes

- Don't expose implementation:
- ❌ `UserImpl` ❌ `FileRepository` ❌ `MemoryCache`
- Instead: ✅ `Users` ✅ `CachedUsers` ✅ `FileUsers`

#### 6. Plural Names = Collections

- Plural name must behave as a collection.
- ```kotlin
- val orders: Orders = OrdersOf(customer)
- orders.first()
- orders.total()
- ```
- Not: `OrderList`, `UserCollection`

#### 7. Use Adjectives for Decorators

- Decorators must be adjectives + noun:
- `EncryptedText`, `CachedUsers`, `LoggedPayments`
- Never: `PaymentLogger`, `CacheManager`

#### 8. No "Data", "DTO", "Bean", "Entity"

- These are anemic, procedural leaks.
- ❌ `UserDTO` ❌ `OrderEntity`
- Objects must contain behavior, not be passive bags of fields.

#### 9. Methods Return Objects, Not Primitives

- `Text name()`, `Money total()`
- Not: `String getName()`, `double getTotal()`

#### 10. Methods Are Verbs Only If They Change State

- Pure methods must still be nouns:
- `order.total()` // noun
- `account.balance()` // noun
- Verbs only for commands:
- `account.transfer(money)`
- `order.cancel()`

#### 11. One Word > Many Words

- Short but precise:
- ❌ `UserAccountBalanceCalculator` → ✅ `Balance`
- ❌ `PasswordEncryptionService` → ✅ `EncryptedPassword`

#### 12. Don't Repeat Context

- ❌ `UserName` ❌ `OrderTotalAmount`
- Context already exists in the object graph.

#### 13. No Boolean "is/has"

- Booleans are also nouns:
- ❌ `isValid()` → ✅ `validity()`
- ❌ `hasAccess()` → ✅ `access()`

#### 14. Constructors Describe "Of", "From", "With"

- `SalaryOf(employee)`
- `FileText(path)`
- `CachedUsers(users)`

### Domain Object Examples

```kotlin
// VPN Domain Objects
class VpnConnection(private val credentials: Credentials) {
    fun establish(): ConnectionResultfun terminate(): DisconnectionResultfun status(): ConnectionStatus
}
class EncryptedCredentials(private val raw: Credentials) {
    fun decrypt(): Credentialsfun validity(): Boolean
}
class Countries(private val source: CountrySource) {
    fun available(): List<Country>fun byCode(code: String): Country
}
class CachedCountries(private val origin: Countries) :
    Countries { // Decorator pattern with adjective + noun
}
```

## Code Organization Principles

### Primary Language

- **Kotlin** is the primary development language
- Leverage Kotlin's features: coroutines, sealed classes, data classes, extension functions

### Business Logic Location

- **Main logic resides in `:domain` submodules**
- Domain objects encapsulate business operations and behavior
- Domain models represent core entities with rich behavior
- Repository interfaces define data contracts

### ViewModel Responsibilities

- **Minimal logic in ViewModels**
- ViewModels serve as state holders and bridges between `:domain` and Compose UI
- Delegate business operations to domain objects
- Handle UI state management and lifecycle awareness

```kotlin
// Example: Minimal ViewModel with Elegant Objects
class VpnViewModel(
    private val vpnConnection: VpnConnection, private val vpnStatus: VpnStatus
) : ViewModel() {
    val status = vpnStatus.observe().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = StableVpnStatus.Disconnected
    )
    fun connect() {
        viewModelScope.launch {
            vpnConnection.establish()
        }
    }
    fun disconnect() {
        viewModelScope.launch {
            vpnConnection.terminate()
        }
    }
}
```

## Presentation Layer Guidelines

### Compose UI

- Use Jetpack Compose for all UI components
- Separate composable functions from business logic
- Implement preview functions for design validation
- Follow Material Design 3 principles

### State Management

- Use `collectAsState()` for observing ViewModel state
- Implement unidirectional data flow
- Handle UI events through lambda parameters

### String Resources Management

- **String resources are allowed ONLY in presentation submodules**
- **All strings MUST be defined in `strings.xml` files**
- **NO hardcoded strings in Compose functions or ViewModel code**
- Use `stringResource(R.string.resource_name)` in Composables
- Pass string resource IDs to ViewModels when needed, not the actual strings
- Domain and data layers should never reference Android string resources

```kotlin
// ❌ Wrong - hardcoded string in Composable
@Composable
fun VpnButton() {
    Button(onClick = {}) {
        Text("Connect to VPN")
    }
}

// ❌ Wrong - hardcoded string in ViewModel
class VpnViewModel {
    private val _status = MutableStateFlow("Disconnected")
}

// ✅ Correct - using string resources
@Composable
fun VpnButton() {
    Button(onClick = {}) {
        Text(stringResource(R.string.vpn_connect_button))
    }
}

// ✅ Correct - ViewModel uses resource IDs
class VpnViewModel {
    private val _statusResId = MutableStateFlow(R.string.vpn_status_disconnected)
    val statusResId: StateFlow<Int> = _statusResId.asStateFlow()
}
```

## Testing Strategy

### Unit Testing

- Test business logic in `:domain` submodules
- Mock external dependencies using interfaces
- Focus on domain object behavior testing

### Integration Testing

- Test repository implementations in `:data` submodules
- Verify data flow between layers

### UI Testing

- Test Compose screens and components
- Use preview functions for visual validation

## File Organization

### Package Structure

```
com.objmobile.{module}
├── data/
│   ├── repository/
│   ├── datasource/
│   └── model/
├── domain/
│   ├── model/
│   ├── repository/
│   └── object/
└── presentation/ (app module only)
    ├── components/
    ├── screen/
    ├── viewmodel/
    └── theme/
```

### Naming Conventions

#### Domain vs Data Layer Naming

- **Domain Models**: Use clean nouns (e.g., `Country`, `User`, `VpnConnection`)
- **Data Models**: Use `BaseModel` suffix (e.g., `CountryBaseModel`, `UserBaseModel`,
  `VpnConnectionBaseModel`)
- **Domain Repositories**: Use clean interface names (e.g., `CountryRepository`, `UserRepository`,
  `VpnRepository`)
- **Data Repositories**: Use `BaseRepository` suffix (e.g., `CountryBaseRepository`,
  `UserBaseRepository`, `VpnBaseRepository`)
- This distinction helps separate business contracts from technical implementations

#### General Naming Rules

- **Domain Objects**: Nouns only (e.g., `VpnConnection`, `EncryptedCredentials`, `Countries`)
- **Domain Repository Interfaces**: `NounRepository` (e.g., `VpnRepository`)
- **Data Repository Implementations**: `NounBaseRepository` (e.g., `VpnBaseRepository`)
- **ViewModels**: `ScreenNameViewModel` (e.g., `VpnViewModel`)
- **Composables**: `NounComposable` (e.g., `VpnScreen`)

#### Layer-Specific Examples

```kotlin
// Domain layer - business contracts and entities
interface CountryRepository {
    suspend fun available(): Countriessuspend fun byCode(code: CountryCode): Country
}
data class Country(
    val name: Text, val code: CountryCode, val flag: Flag
) {
    fun displayName(): Text = namefun isEuropean(): Boolean = code.isEuropean()
}

// Data layer - technical implementations and structures
class CountryBaseRepository(
    private val api: CountryApi, private val cache: CountryCache
) : CountryRepository {
    override suspend fun available(): Countries =
        CachedCountries(ApiCountries(api))
    override suspend fun byCode(code: CountryCode): Country =
        cache.get(code) ?: api.fetchCountry(code)
}
data class CountryBaseModel(
    val name: String, val code: String, val flagUrl: String, val continent: String
)
```

## Error Handling

### Domain Layer

- Use sealed classes for representing different states
- Implement Result wrapper for operations that can fail
- Define domain-specific exceptions

### Data Layer

- Handle network and database exceptions
- Transform external errors to domain errors
- Implement retry mechanisms where appropriate

### Presentation Layer

- Display user-friendly error messages
- Implement loading states
- Handle edge cases gracefully

## Performance Considerations

### Memory Management

- Use appropriate lifecycle scopes for coroutines
- Implement proper resource cleanup
- Avoid memory leaks in ViewModels

### Network Efficiency

- Implement caching strategies in `:data` layer
- Use appropriate HTTP caching headers
- Minimize unnecessary network requests

## Security Guidelines

### VPN Module

- Secure credential storage
- Implement certificate pinning
- Handle sensitive data appropriately

### Permissions Module

- Request permissions at appropriate times
- Provide clear rationale for permission requests
- Handle permission denial gracefully

## Conclusion

These architectural rules ensure a maintainable, testable, and scalable codebase. All team members
should adhere to these principles when contributing to the StableVPN project. Regular code reviews
should verify compliance with these guidelines.

For questions or clarifications about these rules, please refer to this document or discuss with the
team lead.