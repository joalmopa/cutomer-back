# JRE 17 en Alpine: suficiente para ejecutar el JAR (no hace falta el JDK).
FROM eclipse-temurin:17-jre-alpine

# Usuario no-root. En Alpine: addgroup/adduser (BusyBox), no useradd.
RUN addgroup -g 1028 devopsc \
	&& adduser -D -G devopsc admin

WORKDIR /app

# JAR_FILE = nombre del ARCHIVO .jar (Maven: artifactId-version.jar).
# NO es "artifactjar": eso solo es el nombre del paquete en GitHub Actions
# (upload-artifact "name:"). Dentro de ese paquete el fichero sigue siendo
# customer-0.0.1-SNAPSHOT.jar (env ARTIFACT_NAME en publish-jar-as-artifact.yml).
#
# En el workflow, COPY busca ese archivo en el contexto de build (junto al Dockerfile).
# Localmente, desde la raiz del repo:
#   docker build -f .docker/Dockerfile --build-arg JAR_FILE=target/customer-0.0.1-SNAPSHOT.jar -t cutomer-back .
ARG JAR_FILE=customer-0.0.1-SNAPSHOT.jar
ARG DB_PASSWORD

# El JAR va a /app, NO a /tmp. VOLUME /tmp es el directorio temporal de Spring Boot;
# si el jar vive ahi, al arrancar el volumen tapa el archivo y Java no lo encuentra.
COPY ${JAR_FILE} app.jar

# application.properties: db.password=${DB_PASSWORD:12345}
# El workflow pasa este valor como build-arg. Queda en la imagen (ok para el curso).
ENV DB_PASSWORD=$DB_PASSWORD

RUN chown -R admin:devopsc /app

USER admin

VOLUME /tmp

# server.port=8081 en application.properties
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
