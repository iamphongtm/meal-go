# init-new-project

Spring Boot base project for new MealGo services. It contains only shared infrastructure for:

- Jakarta Bean Validation with localized messages.
- Global REST exception handling using `ProblemDetail`.
- A reusable `BusinessException` and `ErrorCode` contract.

It intentionally excludes controllers, business services, entities, repositories, database configuration, and domain-specific error codes.

## Create a service from this base

1. Copy this directory and rename it to the new service name.
2. Update `artifactId`, `name`, and `description` in `pom.xml`.
3. Rename package `com.mealgo.init_new_project` and `InitNewProjectApplication`.
4. Update `spring.application.name` and `server.port` in `application.yaml`.
5. Add service-specific dependencies only when they are needed.
6. Add validation messages to `i18n/messages*.properties`.

## Define business errors

Each service should create its own enum implementing `ErrorCode`:

```java
public enum ServiceErrorCode implements ErrorCode {
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "resource.not-found");

    private final HttpStatus status;
    private final String typeSuffix;

    ServiceErrorCode(HttpStatus status, String typeSuffix) {
        this.status = status;
        this.typeSuffix = typeSuffix;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getTypeSuffix() {
        return typeSuffix;
    }
}
```

Then add matching message keys:

```properties
resource.not-found.title=Resource Not Found
resource.not-found.detail=Could not find resource with ID {0}.
```

Throw it from business code with `new BusinessException(ServiceErrorCode.RESOURCE_NOT_FOUND, id)`.

## Verify

```shell
mvn clean test
```
