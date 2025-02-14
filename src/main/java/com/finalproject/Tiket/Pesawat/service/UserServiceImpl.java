package com.finalproject.Tiket.Pesawat.service;

import com.finalproject.Tiket.Pesawat.dto.SuccesMessageDTO;
import com.finalproject.Tiket.Pesawat.dto.user.request.DeleteUserRequest;
import com.finalproject.Tiket.Pesawat.dto.user.request.UpdateProfileRequest;
import com.finalproject.Tiket.Pesawat.dto.user.request.UploadImageRequest;
import com.finalproject.Tiket.Pesawat.dto.user.response.UpdateProfileResponse;
import com.finalproject.Tiket.Pesawat.dto.user.response.UploadFileResponse;
import com.finalproject.Tiket.Pesawat.dto.user.response.UserDetailsResponse;
import com.finalproject.Tiket.Pesawat.exception.EmailAlreadyRegisteredHandling;
import com.finalproject.Tiket.Pesawat.exception.ExceptionHandling;
import com.finalproject.Tiket.Pesawat.exception.UnauthorizedHandling;
import com.finalproject.Tiket.Pesawat.model.Images;
import com.finalproject.Tiket.Pesawat.model.User;
import com.finalproject.Tiket.Pesawat.repository.UserRepository;
import com.finalproject.Tiket.Pesawat.security.service.UserDetailsImpl;
import com.finalproject.Tiket.Pesawat.utils.Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
public class UserServiceImpl implements UserService {

    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private UserRepository userRepository;

    @Override
    public UploadFileResponse uploadFile(UploadImageRequest uploadImageRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();

            if (!(principal instanceof UserDetailsImpl)) {
                throw new UnauthorizedHandling("User not authenticated");
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            Optional<User> userOptional = userRepository.findByEmailAddress(userDetails.getUsername());

            if (userOptional.isEmpty()) {
                throw new UnauthorizedHandling("User Not Found");
            }

            User user = userOptional.get();

            if (uploadImageRequest.getFile() == null) {
                throw new ExceptionHandling("File is not provided");
            }

            String contentType = uploadImageRequest.getFile().getContentType();

            if (contentType == null) {
                throw new ExceptionHandling("Content type is not provided");
            }

            if (!MediaType.IMAGE_JPEG.isCompatibleWith(MediaType.parseMediaType(contentType)) &&
                    !MediaType.IMAGE_PNG.isCompatibleWith(MediaType.parseMediaType(contentType))) {
                throw new ExceptionHandling("File must be in JPG, JPEG, or PNG format");
            }

            String url = cloudinaryService.uploadFile(uploadImageRequest.getFile(), "user-images");
            if (url == null) {
                throw new ExceptionHandling("Error uploading file");
            }

            Images image = Images.builder()
                    .name(uploadImageRequest.getName())
                    .url(url)
                    .build();

            user.setImages(image);
            userRepository.save(user);

            return UploadFileResponse.builder()
                    .success(true)
                    .urlImage(url)
                    .message("Success upload image")
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ExceptionHandling(e.getMessage());
        }
    }

    @Override
    public UploadFileResponse editFile(MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetailsImpl) {
                Optional<User> userOptional = userRepository
                        .findByEmailAddress(((UserDetailsImpl) principal).getUsername());
                if (userOptional.isEmpty()) {
                    throw new UnauthorizedHandling("User Not Found");
                }
                User user = userOptional.get();

                if (file == null) {
                    throw new ExceptionHandling("File is not provided");
                }

                String contentType = file.getContentType();

                if (contentType == null) {
                    throw new ExceptionHandling("Content type is not provided");
                }

                if (!MediaType.IMAGE_JPEG.isCompatibleWith(MediaType.parseMediaType(contentType)) &&
                        !MediaType.IMAGE_PNG.isCompatibleWith(MediaType.parseMediaType(contentType))) {
                    throw new ExceptionHandling("File must be in JPG, JPEG, or PNG format");
                }
                // Extract public_id from the Cloudinary URL
                String publicId = Utils.extractPublicId(user.getImages().getUrl());
                if (file.isEmpty()) {
                    throw new UnauthorizedHandling("Error Uploading File");
                }
                String url = cloudinaryService.editFile(file, publicId);
                if (url == null) {
                    throw new ExceptionHandling("Error editing file");
                }


                return UploadFileResponse.builder()
                        .success(true)
                        .urlImage(url)
                        .message("Success upload image")
                        .build();
            } else if (principal instanceof String) {
                throw new UnauthorizedHandling("User not authenticated");
            }


        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ExceptionHandling(e.getMessage());
        }

        throw new UnauthorizedHandling("Unknown principal type");
    }

    @Override
    public UpdateProfileResponse editProfile(UpdateProfileRequest updateProfileRequest) {
        // get signed
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetailsImpl) {
                Optional<User> userOptional = userRepository
                        .findByEmailAddress(((UserDetailsImpl) principal).getUsername());
                if (userOptional.isEmpty()) {
                    throw new UnauthorizedHandling("User Not Found");
                }

                User user = userOptional.get();
                user.setFullname(updateProfileRequest.getFullName());
                user.setBirthDate(updateProfileRequest.getDob());
                user.setPhoneNumber(updateProfileRequest.getPhoneNumber());
                user.setLastModified(Utils.getCurrentDateTimeAsDate());
                userRepository.save(user);

            } else if (principal instanceof String) {
                throw new UnauthorizedHandling("User not authenticated");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ExceptionHandling(e.getMessage());
        }
        return UpdateProfileResponse.builder()
                .success(true)
                .message("success update profile")
                .build();
    }

    @Override
    public UserDetailsResponse getUserDetails() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetailsImpl) {
                Optional<User> userOptional = userRepository
                        .findByEmailAddress(((UserDetailsImpl) principal).getUsername());
                if (userOptional.isEmpty()) {
                    throw new UnauthorizedHandling("User Not Found");
                }

                User user = userOptional.get();
                String imageUrl = (user.getImages() != null) ? user.getImages().getUrl() : null;

                return UserDetailsResponse.builder()
                        .success(true)
                        .id(user.getUuid().toString())
                        .email(user.getEmailAddress())
                        .phoneNum(user.getPhoneNumber())
                        .imageUrl(imageUrl)
                        .fullName(user.getFullname())
                        .dob(user.getBirthDate())
                        .roleName(user.getRole().getRoleName().name())
                        .createdAt(user.getCreatedAt())
                        .build();
            } else if (principal instanceof String) {
                throw new UnauthorizedHandling("User not authenticated");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ExceptionHandling(e.getMessage());
        }
        throw new UnauthorizedHandling("Unknown principal type");
    }

    @Override
    public List<User> getAllUser(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);

        if (userPage.isEmpty()) {
            throw new ExceptionHandling("User Empty");
        }

        return userPage.getContent();
    }

    @Override
    public SuccesMessageDTO deleteUserById(DeleteUserRequest deleteUserRequest) {
        try {
            Optional<User> userOptional = userRepository.findById(UUID.fromString(deleteUserRequest.getUserId()));
            if (userOptional.isEmpty()) {
                throw new ExceptionHandling("User Not Found");
            }
            userRepository.delete(userOptional.get());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ExceptionHandling(e.getMessage());
        }
        return SuccesMessageDTO.builder()
                .success(true)
                .message("success delete user")
                .build();
    }


}
