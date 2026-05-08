package com.cardev.hub.service;

import com.cardev.hub.entity.KnowledgeAttachment;
import com.cardev.hub.mapper.KnowledgeAttachmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private final KnowledgeAttachmentMapper attachmentMapper;

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    @Value("${file.allowed-types:pdf,doc,docx,xls,xlsx,ppt,pptx,jpg,jpeg,png,gif,zip,rar,7z}")
    private String allowedTypes;
    
    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;
    
    @Value("${aliyun.oss.accessKeyId:}")
    private String accessKeyId;
    
    @Value("${aliyun.oss.accessKeySecret:}")
    private String accessKeySecret;
    
    @Value("${aliyun.oss.bucketName:}")
    private String bucketName;

    /**
     * 上传文件
     */
    @Transactional
    public KnowledgeAttachment uploadFile(MultipartFile file) throws IOException {
        // 验证文件
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // 验证文件类型
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        if (!allowedTypeList.contains(extension.toLowerCase())) {
            throw new RuntimeException("不支持的文件类型");
        }

        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String newFileName = UUID.randomUUID().toString() + "." + extension;
        String url = "";
        
        if (endpoint != null && !endpoint.isEmpty()) {
            // 使用OSS上传
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            try {
                ossClient.putObject(bucketName, newFileName, file.getInputStream());
                // 生成外网访问URL
                url = "https://" + bucketName + "." + endpoint + "/" + newFileName;
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        } else {
            // 本地上传
            Path targetPath = uploadDir.resolve(newFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 保存附件信息
        KnowledgeAttachment attachment = new KnowledgeAttachment();
        attachment.setFileName(originalFilename);
        attachment.setFilePath(newFileName);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(extension);
        attachment.setDownloadCount(0);
        attachment.setUrl(url);

        attachmentMapper.insert(attachment);

        return attachment;
    }

    /**
     * 上传头像（仅上传文件，不记录到knowledge_attachment表）
     */
    public String uploadAvatar(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // 生成唯一文件名
        String newFileName = "avatar/" + UUID.randomUUID().toString() + "." + extension;

        if (endpoint != null && !endpoint.isEmpty()) {
            // 使用OSS上传
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            try {
                ossClient.putObject(bucketName, newFileName, file.getInputStream());
                return "https://" + bucketName + "." + endpoint + "/" + newFileName;
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        } else {
            // 本地上传
            Path uploadDir = Paths.get(uploadPath, "avatar");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path targetPath = uploadDir.resolve(newFileName.replace("avatar/", ""));
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/avatar/" + targetPath.getFileName().toString();
        }
    }

    /**
     * 下载文件
     */
    public Resource downloadFile(Long attachmentId) throws MalformedURLException {
        KnowledgeAttachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
        }

        Path filePath = Paths.get(uploadPath).resolve(attachment.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("文件不存在");
        }

        // 增加下载次数
        attachment.setDownloadCount(attachment.getDownloadCount() + 1);
        attachmentMapper.updateById(attachment);

        return resource;
    }

    /**
     * 获取附件信息
     */
    public KnowledgeAttachment getAttachment(Long id) {
        return attachmentMapper.selectById(id);
    }

    /**
     * 删除文件
     */
    @Transactional
    public void deleteFile(Long attachmentId) throws IOException {
        KnowledgeAttachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            return;
        }

        // 删除物理文件
        Path filePath = Paths.get(uploadPath).resolve(attachment.getFilePath());
        Files.deleteIfExists(filePath);

        // 删除数据库记录
        attachmentMapper.deleteById(attachmentId);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
