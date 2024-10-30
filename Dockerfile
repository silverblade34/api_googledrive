# Usa una imagen base de Java
FROM openjdk:17-jdk-slim

# Especifica el directorio de trabajo
WORKDIR /app

# Copia el archivo JAR construido en la carpeta /build/libs al contenedor
COPY target/apidrive-0.0.1-SNAPSHOT.jar apidrive.jar

# Copia el archivo credential.json al contenedor
COPY credential.json .

# Expone el puerto que tu aplicación utilizará
EXPOSE 9000

# Comando para ejecutar tu aplicación
ENTRYPOINT ["java", "-jar", "apidrive.jar"]
