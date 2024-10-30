# API Google Drive

Esta API permite interactuar con Google Drive a través de dos endpoints. El primer endpoint permite subir documentos, incluidos imágenes, PDFs y archivos de Word. El segundo endpoint permite subir un PDF y convierte sus páginas en imágenes, que se subirán a Google Drive y se eliminarán automáticamente después de 10 minutos.

## Tecnologías Utilizadas

- Java
- Spring Boot
- Google Drive API
- Maven

## Requisitos Previos

Asegúrate de tener instalados los siguientes requisitos:

- [Java JDK 11 o superior](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Docker](https://www.docker.com/get-started) (opcional)

## Configuración de Google Drive API

1. **Crear un Proyecto en Google Cloud Console**:
   - Visita [Google Cloud Console](https://console.cloud.google.com/).
   - Crea un nuevo proyecto.

2. **Habilitar la API de Google Drive**:
   - En el panel de navegación de la izquierda, ve a **APIs y servicios** > **Biblioteca**.
   - Busca "Google Drive API" y habilítala para tu proyecto.

3. **Crear Credenciales**:
   - Ve a **APIs y servicios** > **Credenciales**.
   - Haz clic en **Crear credenciales** y selecciona **Cuenta de servicio**.
   - Completa los detalles de la cuenta de servicio y haz clic en **Crear**.
   - En la siguiente pantalla, haz clic en **Listo**.
   - Haz clic en el botón de descarga para obtener el archivo `credential.json`. 

4. **Mover el archivo `credential.json`**:
   - Coloca el archivo `credential.json` en la raíz del proyecto.

## Cómo Ejecutar el Proyecto

1. **Clonar el Repositorio**:
   ```bash
   git clone <URL del repositorio>
   cd api_googledrive
   ```

2. **Construir el Proyecto**:
   ```bash
   mvn clean install
   ```

3. **Ejecutar la Aplicación**:
   ```bash
   mvn spring-boot:run
   ```

4. **Acceder a la Aplicación**:
    - Una vez que la aplicación esté en funcionamiento, podrás acceder a ella en `http://localhost:8080`.

## Uso

### Endpoint para Subir Documentos

- **Método**: POST
- **Ruta**: `/upload`
- **Descripción**: Permite subir documentos (imágenes, PDFs y archivos de Word).

### Endpoint para Convertir PDF a Imágenes

- **Método**: POST
- **Ruta**: `/convertPdfToImages`
- **Descripción**: Permite subir un PDF y convierte sus páginas en imágenes, que se subirán a Google Drive y se eliminarán después de 10 minutos. Este endpoint devuelve una lista de links de las imagenes subidas.

## Docker (Opcional)

Si deseas ejecutar la aplicación en un contenedor Docker, sigue estos pasos:

1. **Construir la Imagen**:
   ```bash
   docker build -t api_googledrive .
   ```

2. **Ejecutar el Contenedor**:
   ```bash
   docker run -p 9000:9000 api_googledrive
   ```

## Contribuciones

Si deseas contribuir a este proyecto, por favor crea un **fork** del repositorio y envía un **pull request**.

## Contacto

Si tienes preguntas o comentarios, no dudes en contactar a:

- **Michell Marcos Pacheco Tacay** - [mpacheco.tacay@gmail.com](mailto:mpacheco.tacay@gmail.com)