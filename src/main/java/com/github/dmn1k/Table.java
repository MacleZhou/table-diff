package com.github.dmn1k;


import io.vavr.Function1;
import io.vavr.collection.List;
import lombok.*;

@ToString
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
        Function1<Integer, TableHeader> totalIndexToHeaderFn = headers.withDefaultValue(TableHeader.NON_EXISTING);

        List<TableCell> tableCells =
                List.of(cells)
                        .zipWithIndex()
                        .map(valueToIndex -> valueToIndex.map2(idx -> totalIndexToHeaderFn
                                .apply(idx)
                                .isPrimaryKey()))
                        .map(TableCell::create);


        TableRow row = TableRow.create(adjustCellsToHeaderCount(tableCells));
        return new Table(headers, rows.append(row));
    }

    public List<TableCell> adjustCellsToHeaderCount(List<TableCell> tableCells) {
        List<TableCell> shrinkedCells = tableCells.dropRight(tableCells.size() - headers.size());
        return shrinkedCells.padTo(headers.size(), TableCell.PAD_CELL);
    }
}
