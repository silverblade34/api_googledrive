package com.pdf.apidrive.controllers;

import com.pdf.apidrive.dto.ApiResponse;
import com.pdf.apidrive.services.FileService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;


@Controller
@RequestMapping("/file")
public class FileController {
    @Autowired
    private FileService service;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            if (file.isEmpty()) {
                throw new BadRequestException("File is empty");
            }
            File tempFile = File.createTempFile("temp", null);
            file.transferTo(tempFile);
            String url = service.uploadFileToDrive(tempFile, "UPLOAD", file.getOriginalFilename());
            ApiResponse<String> response = new ApiResponse<>("File saved successfully", url);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/convertPdfToImages")
    public ResponseEntity<ApiResponse<?>> convertPdfToImages(@RequestParam("pdf") MultipartFile file) throws IOException {
        try {
            if (file.isEmpty()) {
                throw new BadRequestException("File is empty");
            }
            File tempFile = File.createTempFile("temp", ".pdf");
            file.transferTo(tempFile);
            List<File> imageFiles = service.convertPdfToImages(tempFile);
            tempFile.delete();

            List<String> urls = service.sendArrayFiles(imageFiles);
            ApiResponse<List<String>> response = new ApiResponse<>("The pdf was converted to images correctly", urls);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
