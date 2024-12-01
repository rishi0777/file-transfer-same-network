package com.example.fileUploadClient.Controller;

import com.example.fileUploadClient.Service.ClientFileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/client")
public class ClientFileController {
    @Autowired
    private ClientFileUploadService clientFileUploadService;

    @Value("${fileTransfer.server.url}")
    private String targetServerUrl;

    @GetMapping("check-connection")
    public ResponseEntity<String> checkConnection() {
        try {
            String response = clientFileUploadService.checkServerConnection(targetServerUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/full-upload")
    public ResponseEntity<String> fullFileUploadToServer(@RequestParam("file") MultipartFile file) {
        try {
            String response = clientFileUploadService.fullFileUpload(targetServerUrl, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFileInChunks(@RequestParam("file") MultipartFile file) {
        try {
            String response = clientFileUploadService.uploadFileInChunks(targetServerUrl, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}

