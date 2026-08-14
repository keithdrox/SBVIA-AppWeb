package com.sbvia.backend.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Página compatible con la serialización JSON de Redis.
 *
 * PageImpl no ofrece un constructor Jackson y falla al recuperar una entrada
 * cacheada. Esta clase conserva el mismo contrato Page y aporta el constructor
 * explícito requerido para reconstruir la paginación.
 */
@JsonIgnoreProperties(
        value = {"pageable", "sort", "first", "last", "empty", "totalPages", "numberOfElements"},
        ignoreUnknown = true)
public class CacheablePage<T> extends PageImpl<T> {

    @JsonCreator
    public CacheablePage(
            @JsonProperty("content") List<T> content,
            @JsonProperty("number") int number,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements) {
        super(content, PageRequest.of(number, Math.max(size, 1)), totalElements);
    }

    public CacheablePage(Page<T> page) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
    }
}
