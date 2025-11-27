package com.sims.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

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
    private boolean last;
    private boolean first;
    private boolean empty;

    /**
     * Constructor for simple list response without pagination metadata
     *
     * @param content list of items
     */
    public PaginatedResponse(List<T> content) {
        this.content = content;
        this.totalElements = content != null ? content.size() : 0;
        this.totalPages = content != null && !content.isEmpty() ? 1 : 0;
        this.last = true;
        this.first = true;
        this.empty = content == null || content.isEmpty();
    }

    /**
     * Factory method to create PaginatedResponse from Spring Data Page
     *
     * @param page Spring Data Page object
     * @param <T> Type of content
     * @return PaginatedResponse with all metadata
     */
    public static <T> PaginatedResponse<T> from(Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isLast(),
                page.isFirst(),
                page.isEmpty()
        );
    }

    /**
     * Factory method for empty paginated response
     */
    public static <T> PaginatedResponse<T> empty() {
        return new PaginatedResponse<>(List.of());
    }
}
