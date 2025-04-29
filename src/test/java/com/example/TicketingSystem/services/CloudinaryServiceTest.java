package com.example.TicketingSystem.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CloudinaryServiceTest {

    @Mock
    private MultipartFile mockFile;

    @Mock
    private Cloudinary mockCloudinary;

    @Mock
    private Uploader mockUploader;

    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockCloudinary.uploader()).thenReturn(mockUploader);
        cloudinaryService = new CloudinaryService(mockCloudinary); // 🔥 No autowiring, just direct injection

    }

    @Test
    void testUploadFile_Success() throws IOException {
        byte[] fileBytes = "test data".getBytes();
        when(mockFile.getBytes()).thenReturn(fileBytes);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://example.com/test-file");
        when(mockUploader.upload(fileBytes, ObjectUtils.emptyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadFile(mockFile);


        assertEquals("https://example.com/test-file", result);
        verify(mockUploader, times(1)).upload(fileBytes, ObjectUtils.emptyMap());
    }

    @Test
    void testUploadFile_ThrowsIOException() throws IOException {
        when(mockFile.getBytes()).thenThrow(new IOException("File read error"));

        IOException exception = assertThrows(IOException.class, () -> cloudinaryService.uploadFile(mockFile));
        assertEquals("File read error", exception.getMessage());
    }
}