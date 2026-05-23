package ru.practicum.utils;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.store.PageableObject;
import ru.practicum.dto.store.SortObject;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class PageableUtils {
    public PageableObject createPageableObject(int page, int size, String[] sort) {
        PageableObject pageable = PageableObject.builder()
                .pageNumber(page)
                .pageSize(size)
                .paged(true)
                .unpaged(false)
                .offset((long) page * size)
                .build();

        List<SortObject> sortObjects = parseSortParameters(sort);

        pageable.setSort(!sortObjects.isEmpty() ? sortObjects.get(0) : null);

        return pageable;
    }

    private List<SortObject> parseSortParameters(String[] sort) {
        List<SortObject> sortList = new ArrayList<>();

        if (sort == null) {
            return sortList;
        }

        for (String sortParam : sort) {
            String[] sortParts = sortParam.split(",");
            String property = sortParts[0];
            String direction = sortParts.length > 1 ? sortParts[1] : "asc";

            SortObject sortObject = SortObject.builder()
                    .property(property)
                    .direction(direction.toUpperCase())
                    .ascending(direction.equalsIgnoreCase("asc"))
                    .ignoreCase(false)
                    .nullHanding("NATIVE")
                    .build();

            sortList.add(sortObject);
        }

        return sortList;
    }
}
