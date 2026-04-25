FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw -f api/pom.xml clean package -DskipTests

CMD ["sh", "-c", "java -jar api/target/*.jar"]