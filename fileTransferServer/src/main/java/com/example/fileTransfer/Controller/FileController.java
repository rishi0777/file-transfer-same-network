package com.example.fileTransfer.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${server.save.location.temp}")
    private String TEMP_DIR;
    @Value("${server.save.location.final}")
    private String FINAL_DIR;

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
            File tempDir = new File(TEMP_DIR);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            File chunkFile = new File(TEMP_DIR + fileName + ".part" + chunkNumber);
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(chunkFile))) {
                outputStream.write(file.getBytes());
            }


            if (chunkNumber == totalChunks - 1) {
                File finalFile = new File(FINAL_DIR + fileName);
                System.out.println(TEMP_DIR + fileName + ".part" + chunkNumber +"  " + FINAL_DIR + fileName);
                try (BufferedOutputStream finalFileStream = new BufferedOutputStream(new FileOutputStream(finalFile))) {
                    for (int i = 0; i < totalChunks; i++) {
                        File partFile = new File(TEMP_DIR + fileName + ".part" + i);
                        byte[] chunkData = Files.readAllBytes(partFile.toPath());
                        finalFileStream.write(chunkData);
                        partFile.delete();
                    }
                }
                return ResponseEntity.ok("File upload complete!");
            }

            return ResponseEntity.ok("Chunk " + chunkNumber + " uploaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("File upload failed");
        }
    }
}
