package com.example.notes.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotesService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {
        // Get the original filename
        String keyName = file.getOriginalFilename();
    
        if (keyName != null) {
            // Convert filename to lower case for case-insensitive matching
            String lowerCaseName = keyName.toLowerCase();
    
            // Define suspicious file extensions and keywords
            String[] disallowedExtensions = { ".exe", ".bat", ".cmd", ".msi", ".com", ".scr", ".vbs", ".js", ".wsf", ".jar", ".sh" };
            String[] disallowedKeywords = { "hack", "hacking", "exploit", "pentest", "malware", "trojan", "keylogger", "backdoor", "ddos", "rootkit", "vulnerability" };
    
            // Check file extension
            for (String ext : disallowedExtensions) {
                if (lowerCaseName.endsWith(ext)) {
                    throw new IOException("Restricted file: illegal extension detected (" + ext + ")");
                }
            }
            
            // Check for suspicious keywords in the filename
            for (String keyword : disallowedKeywords) {
                if (lowerCaseName.contains(keyword)) {
                    throw new IOException("Restricted file: illegal keyword detected (" + keyword + ")");
                }
            }
            
            // Sanitize the filename: trim and replace spaces with underscores
            keyName = keyName.trim().replaceAll(" ", "_");
        } else {
            // Fallback to a default name if the original is null
            keyName = "file_" + System.currentTimeMillis();
        }
        
        // Set up metadata for the file
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        String contentType = file.getContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            contentType = "application/octet-stream";
        }
        metadata.setContentType(contentType);
        
        // Upload the file to S3
        amazonS3.putObject(bucketName, keyName, file.getInputStream(), metadata);
        
        return keyName;
    }
    
    public S3Object downloadFile(String keyName) {
        return amazonS3.getObject(bucketName, keyName);
    }

    public List<String> listNotes(String search) {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName);
        ListObjectsV2Result result;
        List<String> notes = new ArrayList<>();
        do {
            result = amazonS3.listObjectsV2(req);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                // If no search query is provided or if the key contains the search text (case-insensitive), add it
                if (search == null || search.isEmpty() ||
                    objectSummary.getKey().toLowerCase().contains(search.toLowerCase())) {
                    notes.add(objectSummary.getKey());
                }
            }
            req.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
        return notes;
    }
}
