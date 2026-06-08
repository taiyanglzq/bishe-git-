package com.campus.assistant.controller;

import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UploadPathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/**
 * ???? ????????????????????????
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024L;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(400, "图片大小不能超过 5MB");
        }
        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "仅支持 jpg、jpeg、png、webp 格式图片");
        }
        try {
            Path uploadDir = UploadPathUtils.uploadDir();
            Files.createDirectories(uploadDir);
            String filename = UUID.randomUUID() + "." + extension;
            Path target = uploadDir.resolve(filename);
            file.transferTo(target.toFile());
            return Result.success("/uploads/" + filename);
        } catch (IOException ex) {
            throw new BusinessException(500, "图片上传失败");
        }
    }

    private String getExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1).toLowerCase();
    }
}
