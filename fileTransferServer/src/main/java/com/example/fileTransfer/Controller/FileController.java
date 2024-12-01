package com.example.fileTransfer.Controller;

import com.example.fileTransfer.Service.FileTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    FileTransferService fileTransferService;

    @GetMapping("/check-server")
    public String checkConnection(){
        return "File Transfer Server is up and running";
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chunkNumber") int chunkNumber,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("fileName") String fileName) {

        try {
            String response = fileTransferService.handleFileChunk(file, chunkNumber, totalChunks, fileName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }
}
