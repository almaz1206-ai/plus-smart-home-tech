package ru.practicum.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SortObject {
    private String direction;
    private String nullHanding;
    private Boolean ascending;
    private String property;
    private Boolean ignoreCase;
}
