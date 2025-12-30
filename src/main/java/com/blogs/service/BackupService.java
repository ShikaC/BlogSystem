package com.blogs.service;

import com.blogs.dto.ArticleVO;
import com.blogs.entity.Article;
import com.blogs.entity.Comment;
import com.blogs.repository.ArticleRepository;
import com.blogs.repository.CommentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 数据备份服务
 */
@Service
public class BackupService {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 导出所有文章为Markdown格式
     */
    public byte[] exportArticlesAsMarkdown() throws IOException {
        List<Article> articles = articleRepository.findAll();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Article article : articles) {
                String fileName = sanitizeFileName(article.getTitle()) + ".md";
                String content = buildMarkdownContent(article);
                
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                zos.write(content.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        
        return baos.toByteArray();
    }
    
    /**
     * 导出所有文章为HTML格式
     */
    public byte[] exportArticlesAsHtml() throws IOException {
        List<Article> articles = articleRepository.findAll();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Article article : articles) {
                String fileName = sanitizeFileName(article.getTitle()) + ".html";
                String content = buildHtmlContent(article);
                
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                zos.write(content.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        
        return baos.toByteArray();
    }
    
    /**
     * 导出评论数据为JSON
     */
    public byte[] exportComments() throws IOException {
        List<Comment> comments = commentRepository.findAll();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(comments);
    }
    
    /**
     * 导出所有数据
     */
    public byte[] exportAllData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 导出文章
            List<Article> articles = articleRepository.findAll();
            for (Article article : articles) {
                String fileName = "articles/" + sanitizeFileName(article.getTitle()) + ".md";
                String content = buildMarkdownContent(article);
                
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                zos.write(content.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
            
            // 导出评论
            List<Comment> comments = commentRepository.findAll();
            ZipEntry commentEntry = new ZipEntry("comments.json");
            zos.putNextEntry(commentEntry);
            zos.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(comments));
            zos.closeEntry();
        }
        
        return baos.toByteArray();
    }
    
    private String buildMarkdownContent(Article article) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("title: ").append(article.getTitle()).append("\n");
        if (article.getCategory() != null) {
            sb.append("category: ").append(article.getCategory().getName()).append("\n");
        }
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            sb.append("tags: [");
            sb.append(article.getTags().stream()
                    .map(t -> t.getName())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
            sb.append("]\n");
        }
        if (article.getCreatedAt() != null) {
            sb.append("date: ").append(article.getCreatedAt().format(DATE_FORMAT)).append("\n");
        }
        sb.append("---\n\n");
        sb.append(article.getContent() != null ? article.getContent() : "");
        return sb.toString();
    }
    
    private String buildHtmlContent(Article article) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"zh-CN\">\n");
        sb.append("<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <title>").append(article.getTitle()).append("</title>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("  <h1>").append(article.getTitle()).append("</h1>\n");
        sb.append("  <div class=\"content\">\n");
        sb.append(article.getContent() != null ? article.getContent() : "");
        sb.append("\n  </div>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        return sb.toString();
    }
    
    private String sanitizeFileName(String name) {
        if (name == null) return "untitled";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }
}
