# AGENTS.md — TeamsAPI Development Guide

This document defines repository-wide guidance for contributors and coding agents
working in `teams-api`.

---

## Scope

- Applies to the entire repository rooted at this file.

---

## Goals

- Keep the public API (`teams-api/`) minimal, stable, and backward-compatible.
- Never introduce implementation details into the API module.
- Support graceful fallback when no provider is registered.
- Keep the plugin (`teams-api-plugin/`) as a passive bridge with no business logic.

---

## Repository layout

```
teams-api/                     # API module — what third-party plugins depend on
  src/main/java/...
    api/                       # TeamsAPI (facade), TeamsService (interface)
    model/                     # Team, TeamMember, TeamRole
    event/                     # TeamEvent and concrete event classes
  src/test/java/...            # Unit tests for API module only

teams-api-plugin/              # Bukkit plugin module — server-side JAR
  src/main/java/...
    TeamsApiPlugin.java        # Plugin entry point (passive bridge only)
  src/main/resources/
    plugin.yml

docs/                          # Markdown documentation
checkstyle.xml                 # Checkstyle rules (enforced in CI)
jitpack.yml                    # Jitpack build configuration
```

---

## Package roles

| Package  | Purpose                                                           |
|----------|-------------------------------------------------------------------|
| `api`    | Public entry-points. No implementation logic, only contracts.     |
| `model`  | Read-only data interfaces and enums. No Bukkit state.             |
| `event`  | Bukkit event classes. Each concrete event owns its HandlerList.   |

**Never** put implementation classes in `teams-api/`. That module must remain a
clean API artifact.

---

## Structural rules

1. **Interface-first**: Define (or update) the `TeamsService` interface before
   writing or changing implementations.
2. **No implementation in API**: The `teams-api` module must not contain classes
   that `implement TeamsService` or `extend Team`. Those belong in provider plugins.
3. **Optional over null**: All service methods that may not find a result must
   return `Optional<T>`, never `null`.
4. **Null safety**: Public methods in `TeamsAPI` must silently ignore `null`
   arguments (log nothing, return `null`/`false`/empty) rather than throwing.
5. **Events are optional**: Providers are encouraged but not required to fire events.
   The API itself does not fire events.

---

## Code style (Checkstyle)

Rules are defined in `checkstyle.xml`. All violations block the CI `verify` step.
The most critical rules:

- **No star imports** (`AvoidStarImport`). Organize by group with blank lines.
- **Javadoc required** on every `public` type and method with all `@param` tags.
- **4-space indent**, no tabs (`FileTabCharacter`, `Indentation`).
- **Max 120 characters** per line.
- **`final` on local variables** that are never reassigned (`FinalLocalVariable`).
- **Braces always** around `if`, `else`, `for`, `while` bodies (`NeedBraces`).
- **Opening brace on same line** (`LeftCurly(eol)`).
- **Closing brace alone** on its own line. Never `} catch (…) {` or `} else {`
  on the same line (`RightCurly(alone)`):

```java
// WRONG
} catch (Exception e) {
} else {

// CORRECT
}
catch (Exception e) {
}
else {
```

- **Blank line between declarations** — between every method, field, and
  constructor (`EmptyLineSeparator`).
- **Import order** — alphabetical within each group, blank line between groups:

```java
// CORRECT
import com.skyblockexp.teamsapi.api.TeamsService;
import com.skyblockexp.teamsapi.model.Team;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
```

---

## Build & test expectations

```bash
# Compile all modules
mvn -B -DskipTests compile

# Run tests (API module only has tests)
mvn -B -pl teams-api test

# Checkstyle check
mvn -B -pl teams-api checkstyle:check

# Full verify (compile + test + checkstyle)
mvn -B verify

# Package the server plugin JAR
mvn -B -DskipTests package
```

All three must pass (zero style violations, zero test failures) before any
change is considered complete.

---

## Tests

- Tests live in `teams-api/src/test/java/`.
- Use MockBukkit for any test that requires a Bukkit environment.
- Use Arrange / Act / Assert structure.
- Test method naming: `<methodName>_<scenario>_<expected>` (e.g.,
  `isAvailable_returnsFalse_whenNoProviderRegistered`).
- Every test must have a clear Javadoc comment stating what it verifies.
- No coverage-only tests. Every test must assert concrete behaviour.

---

## API versioning

When making breaking changes to `TeamsService` or `Team`:

1. Bump `TeamsAPI.API_VERSION` (follows Semantic Versioning).
2. Update the version in `pom.xml` (root and modules) consistently.
3. Document the change in `docs/api.md` under a "Migration" section.

Non-breaking additions (new default methods, new optional methods) do not require
a major version bump.

---

## PR readiness checklist

- [ ] Code compiles (`mvn compile`).
- [ ] Tests pass (`mvn test`).
- [ ] Checkstyle passes (`mvn checkstyle:check -pl teams-api`).
- [ ] New public API has full Javadoc with `@param` tags.
- [ ] No implementation classes were added to `teams-api/`.
- [ ] `TeamsAPI.API_VERSION` updated if the change is breaking.
- [ ] Relevant docs in `docs/` updated.
