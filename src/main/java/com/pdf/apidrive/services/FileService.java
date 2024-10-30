package com.pdf.apidrive.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.apache.coyote.BadRequestException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class FileService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SERVICE_ACOUNT_KEY_PATH = getPathToGoodleCredentials();
    private final Map<String, Long> temporaryStorage = new HashMap<>();

    private static String getPathToGoodleCredentials() {
        String currentDirectory = System.getProperty("user.dir");
        Path filePath = Paths.get(currentDirectory, "credential.json");
        return filePath.toString();
    }

    public String uploadFileToDrive(File file, String type, String fileName) {
        try {
            String folderId = type.equals("CONVERT") ? "1SEfZotf96NFnbjCwmevO_fQnHxo9zEXS" : "1aeV8dYjCTLdlq4qkQGvLdlwQkfzNtYQG";
            Drive drive = createDriveService();

            com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
            fileMetaData.setName(file.getName());
            fileMetaData.setParents(Collections.singletonList(folderId));

            String mimeType;
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                mimeType = "image/jpeg";
            } else if (fileName.endsWith(".pdf")) {
                mimeType = "application/pdf";
            } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else {
                throw new IllegalArgumentException("Tipo de archivo no soportado");
            }

            FileContent mediaContent = new FileContent(mimeType, file);
            com.google.api.services.drive.model.File uploadFile = drive.files().create(fileMetaData, mediaContent).setFields("id, createdTime").execute();
            if (type.equals("CONVERT")) {
                addToTemporaryStorage(uploadFile.getId(), uploadFile.getCreatedTime().getValue());
                System.out.println("Archivo subido y guardado en temporaryStorage: " + uploadFile.getId());
            } else {
                System.out.println("Archivo subido sin guardar en temporaryStorage: " + uploadFile.getId());
            }
            file.delete();
            return "https://drive.google.com/uc?export=view&id=" + uploadFile.getId();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
    }

    private Drive createDriveService() throws GeneralSecurityException, IOException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_ACOUNT_KEY_PATH))
                .createScoped(Collections.singleton(DriveScopes.DRIVE));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential).build();
    }

    public List<File> convertPdfToImages(File pdfFile) throws IOException {
        List<File> imageFiles = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage originalImage = pdfRenderer.renderImageWithDPI(page, 100);

                // Reducir la profundidad de color a 24 bits
                BufferedImage reducedColorImage = new BufferedImage(
                        originalImage.getWidth(),
                        originalImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = reducedColorImage.createGraphics();
                g2d.drawImage(originalImage, 0, 0, null);
                g2d.dispose();

                File imageFile = File.createTempFile("page_" + (page + 1), ".jpg");

                // Compresión progresiva JPEG
                ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                jpgWriteParam.setCompressionQuality(0.6f);
                jpgWriteParam.setProgressiveMode(ImageWriteParam.MODE_DEFAULT); // Compresión progresiva

                try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(imageFile)) {
                    jpgWriter.setOutput(outputStream);
                    jpgWriter.write(null, new IIOImage(reducedColorImage, null, null), jpgWriteParam);
                }
                jpgWriter.dispose();
                imageFiles.add(imageFile);
            }
        }
        return imageFiles;
    }

    public List<String> sendArrayFiles(List<File> files) {
        List<CompletableFuture<String>> futures = files.stream()
                .map(imageFile -> CompletableFuture.supplyAsync(() -> {
                    System.out.println("Subiendo archivo: " + imageFile.getName());
                    String urlFile = uploadFileToDrive(imageFile, "CONVERT", imageFile.getName());
                    imageFile.delete();
                    return urlFile;
                }))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public void addToTemporaryStorage(String fileId, long createdTime) {
        temporaryStorage.put(fileId, createdTime);
    }

    @Scheduled(fixedRate = 60000)
    public void deleteOldFiles() {
        long currentTime = System.currentTimeMillis();
        temporaryStorage.entrySet().removeIf(entry -> {
            long fileAge = currentTime - entry.getValue();
            if (fileAge >= 10 * 60 * 1000) {
                deleteFileFromDrive(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public void deleteFileFromDrive(String fileId) {
        try {
            Drive drive = createDriveService();
            drive.files().delete(fileId).execute();
        } catch (Exception e) {
            System.out.println("Error deleting file: " + e.getMessage());
        }
    }

    public File getTempFileFromLink(String linkPdf) throws IOException {
        URL url = new URL(linkPdf);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/pdf");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download PDF: " + responseCode);
        }

        File tempFile = File.createTempFile("temp", ".pdf");
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
}
