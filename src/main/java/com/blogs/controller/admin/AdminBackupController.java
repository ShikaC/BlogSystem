package com.blogs.controller.admin;

import com.blogs.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 后台数据备份控制器
 */
@RestController
@RequestMapping("/admin/backup")
public class AdminBackupController {

    @Autowired
    private BackupService backupService;

    /**
     * 导出文章为Markdown
     */
    @GetMapping("/articles/markdown")
    public ResponseEntity<byte[]> exportArticlesAsMarkdown() throws IOException {
        byte[] data = backupService.exportArticlesAsMarkdown();
        String fileName = "articles_md_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ".zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    /**
     * 导出文章为HTML
     */
    @GetMapping("/articles/html")
    public ResponseEntity<byte[]> exportArticlesAsHtml() throws IOException {
        byte[] data = backupService.exportArticlesAsHtml();
        String fileName = "articles_html_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ".zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    /**
     * 导出评论
     */
    @GetMapping("/comments")
    public ResponseEntity<byte[]> exportComments() throws IOException {
        byte[] data = backupService.exportComments();
        String fileName = "comments_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ".json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    /**
     * 导出所有数据
     */
    @GetMapping("/all")
    public ResponseEntity<byte[]> exportAllData() throws IOException {
        byte[] data = backupService.exportAllData();
        String fileName = "blog_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ".zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
