package ru.practicum.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageableObject {
    private Long offset;
    private SortObject sort;
    private Boolean unpaged;
    private Boolean paged;
    private Integer pageNumber;
    private Integer pageSize;
}
