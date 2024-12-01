package com.example.fileUploadClient.Service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ClientFileUploadService {

    private static final int CHUNK_SIZE = 100 * 1024 * 1024;

    public String checkServerConnection(String targetServerUrl) throws Exception {
        String serverUrl = targetServerUrl + "/check-server";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(serverUrl, HttpMethod.GET, null, String.class);
        return response.getBody();
    }

    public String fullFileUpload(String targetServerUrl, MultipartFile file) throws IOException {
        String serverUrl = targetServerUrl + "/upload";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource byteArrayResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(serverUrl, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    public String uploadFileInChunks(String targetServerUrl, MultipartFile file) throws IOException {
        String SERVER_URL = targetServerUrl + "/upload";
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();
        int totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);

        RestTemplate restTemplate = new RestTemplate();

        System.out.println("File upload started, total chunks to upload " + totalChunks);
        for (int i = 0; i < totalChunks; i++) {
            byte[] chunkData = getFileChunk(file, i);

            File chunkFile = new File(fileName + ".part" + i);
            try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
                fos.write(chunkData);
            }

            FileSystemResource chunkFileResource = new FileSystemResource(chunkFile);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", chunkFileResource);
            body.add("chunkNumber", i);
            body.add("totalChunks", totalChunks);
            body.add("fileName", fileName);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(SERVER_URL,
                    HttpMethod.POST,
                    entity,
                    String.class);
            System.out.println(response.getBody());
            chunkFile.delete();
        }

//        String fileName = file.getOriginalFilename();
//        long fileSize = file.getSize();
//        int totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
//
//        RestTemplate restTemplate = new RestTemplate();
//        for (int i = 0; i < totalChunks; i++) {
//            byte[] chunkData = getFileChunk(file, i);
//
//            File chunkFile = new File(fileName + ".part" + i);
//            try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
//                fos.write(chunkData);
//            }
//
//            FileSystemResource chunkFileResource = new FileSystemResource(chunkFile);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            body.add("file", chunkFileResource);
//            body.add("chunkNumber", i);
//            body.add("totalChunks", totalChunks);
//            body.add("fileName", fileName);
//
//            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
//
//            ResponseEntity<String> response = restTemplate.exchange(serverUrl,
//                    HttpMethod.POST,
//                    entity,
//                    String.class);
//            System.out.println(response.getBody());
//            chunkFile.delete();
//        }

        return "File upload complete!";
    }

    public byte[] getFileChunk(MultipartFile file, int chunkNumber) throws IOException {
        long start = (long) chunkNumber * CHUNK_SIZE;
        long end = Math.min(start + CHUNK_SIZE, file.getSize());

        byte[] chunkData = new byte[(int) (end - start)];
        try (InputStream inputStream = file.getInputStream()) {
            inputStream.skip(start);
            inputStream.read(chunkData);
        }

        return chunkData;
    }
}
