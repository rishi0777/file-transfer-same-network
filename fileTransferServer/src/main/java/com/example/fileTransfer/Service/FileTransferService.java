package com.example.fileTransfer.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class FileTransferService {
    @Value("${server.save.location.temp}")
    private String TEMP_DIR;
    @Value("${server.save.location.final}")
    private String FINAL_DIR;

    public String handleFileChunk(MultipartFile file, int chunkNumber, int totalChunks, String fileName) throws IOException {
        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File chunkFile = new File(TEMP_DIR + fileName + ".part" + chunkNumber);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(chunkFile))) {
            outputStream.write(file.getBytes());
        }

        if (chunkNumber == totalChunks - 1) {
            assembleFile(fileName, totalChunks);
            return "File upload complete!";
        }

        return "Chunk " + chunkNumber + " uploaded successfully";
    }

    private void assembleFile(String fileName, int totalChunks) throws IOException {
        File finalDir = new File(FINAL_DIR);
        if (!finalDir.exists()) {
            finalDir.mkdirs();
        }

        File finalFile = new File(FINAL_DIR + fileName);
        try (BufferedOutputStream finalFileStream = new BufferedOutputStream(new FileOutputStream(finalFile))) {
            for (int i = 0; i < totalChunks; i++) {
                File partFile = new File(TEMP_DIR + fileName + ".part" + i);
                byte[] chunkData = Files.readAllBytes(partFile.toPath());
                finalFileStream.write(chunkData);
                partFile.delete();
            }
        }
    }
}
