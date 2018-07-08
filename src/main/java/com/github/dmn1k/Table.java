package com.github.dmn1k;


import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Table {
    private List<TableHeader> headers;
    private List<TableRow> rows;

    public static Table create(TableHeader... headers) {
        return new Table(List.of(headers), List.empty());
    }

    public Table addRow(String... cells) {
        List<TableCell> tableCells =
                List.of(cells)
                        .zipWithIndex()
                        .map(valueToIndex -> valueToIndex.map2(idx -> headers.get(idx).isPrimaryKey()))
                        .map(TableCell::create);

        return new Table(headers, rows.append(TableRow.create(tableCells)));
    }
}
