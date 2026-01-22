FROM eclipse-temurin:21-jdk

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]