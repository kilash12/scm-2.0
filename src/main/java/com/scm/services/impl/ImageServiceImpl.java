package com.scm.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.scm.helpers.AppConstants;
import com.scm.services.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);

    private final Cloudinary cloudinary;

    public ImageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(MultipartFile contactImage, String filename) {
        try {
            // Read the file bytes
            byte[] data = contactImage.getBytes();

            // Upload to Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(data, ObjectUtils.asMap(
                    "public_id", filename,
                    "folder", "scm_contacts"       // optional: store in a folder
            ));

            logger.info("Image uploaded successfully: {}", uploadResult.get("public_id"));
            return getUrlFromPublicId(filename);
        } catch (IOException e) {
            logger.error("Failed to read image file: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Cloudinary upload failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String getUrlFromPublicId(String publicId) {
        return cloudinary
                .url()
                .transformation(new Transformation<>()
                        .width(AppConstants.CONTACT_IMAGE_WIDTH)
                        .height(AppConstants.CONTACT_IMAGE_HEIGHT)
                        .crop("thumb")                 // <-- change from "fill" to "thumb"
                        .gravity("face")               // focus on the face if detected
                        .quality("auto")               // optimize quality
                        .fetchFormat("auto"))          // serve WebP if supported
                .generate(publicId);
    }
}