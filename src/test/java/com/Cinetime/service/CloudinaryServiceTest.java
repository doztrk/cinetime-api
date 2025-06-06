// Create this test to verify your setup works
// Put this in src/test/java/com/Cinetime/service/CloudinaryServiceTest.java

package com.Cinetime.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Optional: use test profile
public class CloudinaryServiceTest {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Test
    public void testCloudinaryInitialization() {
        // This test will pass if Cloudinary initializes correctly
        // If your credentials are wrong, the @PostConstruct method will fail
        System.out.println("Cloudinary service initialized successfully!");
    }
}