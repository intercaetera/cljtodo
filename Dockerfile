FROM clojure:temurin-17-lein AS builder
WORKDIR /app
COPY project.clj /app/
RUN lein deps
COPY . /app
RUN lein clean
RUN lein uberjar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*-standalone.jar /app/app.jar
EXPOSE 3000
CMD ["java", "-jar", "/app/app.jar"]
