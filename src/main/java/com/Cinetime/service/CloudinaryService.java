package com.Cinetime.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Cloudinary service for movie poster management
 * Handles upload, update, and deletion of movie images
 */
@Service
@Slf4j
public class CloudinaryService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));

        log.info("Cloudinary initialized successfully for cloud: {}", cloudName);
    }

    /**
     * Upload movie poster with automatic optimization
     *
     * @param file    The image file to upload
     * @param movieId The movie ID for naming
     * @return The secure URL of the uploaded image
     * @throws IOException if upload fails
     */
    public String uploadMoviePoster(MultipartFile file, Long movieId) throws IOException {
        validateFile(file);

        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", "cinetime/movie-posters",
                    "public_id", "movie_" + movieId + "_" + System.currentTimeMillis(),
                    "overwrite", false,
                    "resource_type", "image",
                    // Fix: Use proper transformation format for Cloudinary Java SDK
                    "width", 500,
                    "height", 750,
                    "crop", "fill",
                    "quality", "auto",
                    "fetch_format", "auto",
                    "tags", Arrays.asList("movie", "poster", "cinetime"));

            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String imageUrl = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");

            log.info("Successfully uploaded movie poster - ID: {}, URL: {}, PublicID: {}",
                    movieId, imageUrl, publicId);

            return imageUrl;

        } catch (Exception e) {
            log.error("Failed to upload movie poster for ID: {}", movieId, e);
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Delete movie poster from Cloudinary
     *
     * @param imageUrl The URL of the image to delete
     * @return true if deletion was successful or if file didn't exist
     */
    public boolean deleteMoviePoster(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            log.debug("No image URL provided for deletion");
            return true;
        }

        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId == null) {
                log.warn("Could not extract public ID from URL: {}", imageUrl);
                return false;
            }

            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");

            boolean success = "ok".equals(resultStatus) || "not found".equals(resultStatus);

            if (success) {
                log.info("Successfully processed deletion for image: {} (status: {})", publicId, resultStatus);
            } else {
                log.warn("Unexpected deletion result for {}: {}", publicId, result);
            }

            return success;

        } catch (Exception e) {
            log.error("Error deleting image from Cloudinary: {}", imageUrl, e);
            return false;
        }
    }

    /**
     * Update movie poster - uploads new image and deletes old one
     *
     * @param newFile     The new image file
     * @param movieId     The movie ID
     * @param oldImageUrl The URL of the old image to delete
     * @return The URL of the new uploaded image
     * @throws IOException if upload fails
     */
    public String updateMoviePoster(MultipartFile newFile, Long movieId, String oldImageUrl) throws IOException {
        // Upload new image first (fail fast if this doesn't work)
        String newImageUrl = uploadMoviePoster(newFile, movieId);

        // Delete old image (don't fail the update if this fails)
        if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
            boolean deleted = deleteMoviePoster(oldImageUrl);
            if (!deleted) {
                log.warn("Failed to delete old image during update: {}", oldImageUrl);
                // Continue anyway - we have the new image uploaded
            }
        }

        return newImageUrl;
    }

    /**
     * Validate uploaded file meets our requirements
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Check file size (10MB max)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    String.format("File size (%d bytes) exceeds maximum allowed size (%d bytes)",
                            file.getSize(), maxSize));
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("File content type cannot be determined");
        }

        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image. Provided type: " + contentType);
        }

        // Check specific image types
        String[] allowedTypes = {"image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"};
        boolean validType = false;
        for (String allowedType : allowedTypes) {
            if (allowedType.equals(contentType)) {
                validType = true;
                break;
            }
        }

        if (!validType) {
            throw new IllegalArgumentException(
                    "Invalid image type: " + contentType + ". Allowed types: JPEG, PNG, WebP, GIF");
        }
    }

    /**
     * Extract Cloudinary public ID from the image URL
     * This is needed for deletion operations
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // Cloudinary URL format:
            // https://res.cloudinary.com/{cloud_name}/image/upload/{transformations}/{version}/{public_id}.{format}

            if (!imageUrl.contains("cloudinary.com")) {
                log.warn("URL doesn't appear to be a Cloudinary URL: {}", imageUrl);
                return null;
            }

            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                log.warn("URL doesn't contain '/upload/': {}", imageUrl);
                return null;
            }

            String pathPart = parts[1];

            // Remove transformations if present (they start with parameters)
            String[] pathSegments = pathPart.split("/");
            StringBuilder publicIdBuilder = new StringBuilder();

            boolean foundVersion = false;
            for (String segment : pathSegments) {
                // Skip transformation parameters (contain = or start with letters like w_, h_, c_)
                if (segment.contains("=") || segment.matches("^[a-z]_.*")) {
                    continue;
                }

                // Skip version (starts with 'v' followed by numbers)
                if (segment.matches("^v\\d+$")) {
                    foundVersion = true;
                    continue;
                }

                // This should be part of the public ID
                if (publicIdBuilder.length() > 0) {
                    publicIdBuilder.append("/");
                }
                publicIdBuilder.append(segment);
            }

            String publicId = publicIdBuilder.toString();

            // Remove file extension from the last segment
            int lastSlash = publicId.lastIndexOf('/');
            String lastSegment = lastSlash >= 0 ? publicId.substring(lastSlash + 1) : publicId;
            int dotIndex = lastSegment.lastIndexOf('.');

            if (dotIndex > 0) {
                String withoutExtension = lastSegment.substring(0, dotIndex);
                if (lastSlash >= 0) {
                    publicId = publicId.substring(0, lastSlash + 1) + withoutExtension;
                } else {
                    publicId = withoutExtension;
                }
            }

            log.debug("Extracted public ID '{}' from URL '{}'", publicId, imageUrl);
            return publicId;

        } catch (Exception e) {
            log.error("Failed to extract public ID from URL: {}", imageUrl, e);
            return null;
        }
    }
}