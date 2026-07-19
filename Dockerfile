# --- Стадия 1: сборка ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Сначала копируем только pom.xml и качаем зависимости.
# Пока pom.xml не меняется, Docker переиспользует этот слой из кэша,
# и пересборка после правок в src проходит намного быстрее.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Теперь копируем исходники и собираем jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Стадия 2: запуск ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Непривилегированный пользователь — не запускать приложение от root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/payment-processing-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
