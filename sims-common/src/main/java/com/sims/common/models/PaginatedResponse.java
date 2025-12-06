package com.sims.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Standard paginated response for all SIMS microservices
 * Wraps Spring Data Page into a consistent DTO
 *
 * @param <T> Type of content items
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;

    /**
     * Constructor for simple list response without pagination metadata
     *
     * @param content list of items
     */
    public PaginatedResponse(List<T> content) {
        this.content = content;
        this.totalElements = content != null ? content.size() : 0;
        this.totalPages = content != null && !content.isEmpty() ? 1 : 0;
        this.currentPage = 0;
        this.pageSize = 0;
    }

    /**
     * Constructor to create PaginatedResponse from Spring Data Page
     *
     * @param page Spring Data Page object
     * @return PaginatedResponse with all metadata
     */
    public PaginatedResponse(Page<T> page) {
        this.content = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
    }

    /**
     * Factory method for empty paginated response
     */
    public static <T> PaginatedResponse<T> empty() {
        return new PaginatedResponse<>(List.of());
    }
}
