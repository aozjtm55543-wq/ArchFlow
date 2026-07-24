FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
