package com.blogs.service;

import com.blogs.common.PageResult;
import com.blogs.entity.Media;
import com.blogs.exception.BusinessException;
import com.blogs.repository.MediaRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 媒体文件服务
 */
@Service
@Transactional
public class MediaService {
    
    @Value("${upload.path}")
    private String uploadPath;
    
    @Autowired
    private MediaRepository mediaRepository;
    
    /**
     * 上传文件
     */
    public Media uploadFile(MultipartFile file, String category) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        
        String originalName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalName);
        String fileName = UUID.randomUUID().toString() + "." + extension;
        
        // 按日期分目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path dirPath = Paths.get(uploadPath, dateDir);
        
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        
        Path filePath = dirPath.resolve(fileName);
        Files.write(filePath, file.getBytes());
        
        // 保存到数据库
        Media media = new Media();
        media.setOriginalName(originalName);
        media.setFileName(fileName);
        media.setFilePath(filePath.toString());
        media.setFileUrl("/uploads/" + dateDir + "/" + fileName);
        media.setFileType(file.getContentType());
        media.setFileSize(file.getSize());
        media.setCategory(category);
        
        return mediaRepository.save(media);
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(Long id) throws IOException {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文件不存在"));
        
        Path filePath = Paths.get(media.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        mediaRepository.delete(media);
    }
    
    /**
     * 获取媒体列表
     */
    public PageResult<Media> getMediaList(String type, String category, String keyword, 
                                          Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Media> mediaPage;
        
        if (keyword != null && !keyword.isEmpty()) {
            mediaPage = mediaRepository.findByOriginalNameContainingOrderByCreatedAtDesc(keyword, pageable);
        } else if (category != null && !category.isEmpty()) {
            mediaPage = mediaRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
        } else if (type != null && !type.isEmpty()) {
            mediaPage = mediaRepository.findByFileTypeContainingOrderByCreatedAtDesc(type, pageable);
        } else {
            mediaPage = mediaRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        return PageResult.of(mediaPage.getContent(), mediaPage.getTotalElements(), page, size);
    }
}
