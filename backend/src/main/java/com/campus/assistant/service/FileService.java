package com.campus.assistant.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口，定义图片上传相关业务能力。
 */
public interface FileService {

    String upload(MultipartFile file);
}
