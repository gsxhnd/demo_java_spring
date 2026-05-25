# AGENTS.md

## Repo structure

This is a **collection of independent Spring Boot example projects**, not a single application. There is no root `pom.xml` and no multi-module Maven build.

```
examples/          # standalone Spring Boot example projects, each with its own pom.xml
devops/            # Docker Compose files for middleware (MySQL, Redis, MongoDB, etc.)
docs/              # Chinese-primary documentation (dev guides, wiki, usage, reference)
.agents/skills/    # AI agent skill definitions
```

Each project under `examples/` must be independently compilable and runnable. No cross-project dependencies are allowed.

## Version matrix

- Java 21, Spring Boot 4.0.5, Spring Framework 7.0.6, Maven 3.9+
- No Maven wrapper (`mvnw`) is committed in any example project
- All examples use `spring-boot-starter-parent:4.0.5` as parent POM

## Commands

All commands must be run from within a specific example project directory:

```bash
# Run an example (from its directory, e.g. examples/spring-mvc-demo/)
mvn spring-boot:run

# Compile
mvn compile

# Package (skip tests — no test code exists yet)
mvn package -DskipTests

# Run tests (when they exist)
mvn test
```

Middleware setup (from `devops/`):

```bash
# Start all middleware
docker compose -f devops/full-stack-compose.yml up -d

# Start only what you need
docker compose -f devops/full-stack-compose.yml up -d mysql redis

# Stop all
docker compose -f devops/full-stack-compose.yml down
```

## Conventions

- **Package structure:** `com.example.{topic}` with sub-packages: `controller/`, `service/`, `repository/`, `entity/`, `dto/`, `config/`, `exception/`
- **Entrypoint class:** `{Topic}DemoApplication.java` annotated with `@SpringBootApplication`
- **Config format:** YAML only (`application.yml`), no `.properties`
- **All projects run on port 8080**
- **Lombok** is used in all projects (mark as `optional: true` in pom.xml, exclude from `spring-boot-maven-plugin`)
- **Constructor injection** with `private final` fields (no `@Autowired` on fields)
- **GroupId:** `com.example`, **ArtifactId:** `spring-{topic}-demo`
- **JPA `open-in-view: false`** is explicitly set where JPA is used

## Current gaps

- No test source files exist in any project
- No CI/CD pipeline
- No code formatters or linters configured
- Dockerfile references `./mvnw` but no wrapper is committed

## Middleware credentials (dev defaults)

| Service       | Port  | User/Pass                |
|---------------|-------|--------------------------|
| MySQL         | 3306  | root / root123           |
| PostgreSQL    | 5432  | postgres / postgres123   |
| Redis         | 6379  | password: redis123       |
| MongoDB       | 27017 | root / mongo123          |
| Elasticsearch | 9200  | (security disabled)      |
| ClickHouse    | 8123  | default / clickhouse123  |
| InfluxDB      | 8086  | admin / admin12345678    |

All connect to database `demo_db` (except InfluxDB: bucket `monitoring`, org `my-org`).
