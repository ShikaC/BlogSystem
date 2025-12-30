package com.blogs.dto;

import lombok.Data;

import java.util.List;

/**
 * 时间归档DTO
 */
@Data
public class ArchiveDTO {
    private Integer year;
    private Integer month;
    private Long count;
    private List<ArticleVO> articles;
    
    public static ArchiveDTO of(Integer year, Integer month, Long count) {
        ArchiveDTO dto = new ArchiveDTO();
        dto.setYear(year);
        dto.setMonth(month);
        dto.setCount(count);
        return dto;
    }
}
