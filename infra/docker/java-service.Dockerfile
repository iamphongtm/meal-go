# Shared Java service image; build with the repository root as context.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
ARG SERVICE_PATH
COPY pom.xml ./pom.xml
COPY common-web ./common-web
COPY ${SERVICE_PATH} ./service
# Keep the parent in place while building the selected module in isolation.
# This avoids resolving unrelated reactor modules (search is not in the root reactor).
RUN sed -i 's|<relativePath>../../pom.xml</relativePath>|<relativePath>../pom.xml</relativePath>|g' service/pom.xml \
    && mvn -B -N install -DskipTests \
    && mvn -B -f common-web/pom.xml install -DskipTests \
    && mvn -B -f service/pom.xml package -DskipTests \
    && cp service/target/*-SNAPSHOT.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app && useradd --system --gid app --home-dir /app app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/app.jar ./app.jar
USER app
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Duser.timezone=Asia/Ho_Chi_Minh"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
