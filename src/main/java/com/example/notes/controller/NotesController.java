package com.example.notes.controller;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.example.notes.service.NotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NotesController {

    @Autowired
    private NotesService notesService;

    // Upload multiple files (or a folder containing files)
    @PostMapping("/upload")
    public ResponseEntity<String> uploadNotes(@RequestParam("files") MultipartFile[] files) {
        StringBuilder message = new StringBuilder();
        try {
            for (MultipartFile file : files) {
                String keyName = notesService.uploadFile(file);
                message.append("Uploaded: ").append(keyName).append("\n");
            }
            return ResponseEntity.ok(message.toString());
        } catch (IOException e) {
            // Return a 400 Bad Request if a file is restricted
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }
    

    // List (and search) files by filename
    @GetMapping
    public ResponseEntity<List<String>> listNotes(@RequestParam(value = "search", required = false) String search) {
        List<String> files = notesService.listNotes(search);
        return ResponseEntity.ok(files);
    }

    // Download a file by its key name
    @GetMapping("/download/{keyName}")
    public ResponseEntity<byte[]> downloadNote(@PathVariable String keyName) {
        try {
            S3Object s3Object = notesService.downloadFile(keyName);
            byte[] content = IOUtils.toByteArray(s3Object.getObjectContent());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(keyName)
                    .build());
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
