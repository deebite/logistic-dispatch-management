FROM eclipse-temurin:25-jdk

WORKDIR /dispatch-management

COPY target/dispatch-management-0.0.1-SNAPSHOT.jar dispatch-management.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "dispatch-management.jar"]