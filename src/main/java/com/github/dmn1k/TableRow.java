package com.github.dmn1k;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;

import java.util.Arrays;
import java.util.List;

@Builder
@EqualsAndHashCode
public class TableRow {
    @Singular
    private List<String> cells;

    public static TableRow create(String... cells) {
        return TableRow.builder()
                .cells(Arrays.asList(cells))
                .build();

    }

    public io.vavr.collection.List<String> cells() {
        return io.vavr.collection.List.ofAll(cells);
    }

}
