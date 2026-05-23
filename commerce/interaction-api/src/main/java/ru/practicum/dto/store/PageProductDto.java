package ru.practicum.dto.store;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageProductDto {
    private Long totalElements;
    private Integer totalPages;
    private Boolean first;
    private Boolean last;
    private Integer size;
    private List<ProductDto> content;
    private Integer number;
    private List<SortObject> sort;
    private Integer numberOfElements;
    private PageableObject pageable;
    private Boolean empty;
}
