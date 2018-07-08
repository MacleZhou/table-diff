package com.github.dmn1k;


import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.*;

@ToString
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Table {
    private List<TableHeader> headers;
    private List<TableRow> rows;

    public static Table create(List<TableHeader> headers) {
        return new Table(headers, List.empty());
    }

    public static Table create(TableHeader... headers) {
        return create(List.of(headers));
    }

    public Table addRow(String... cells) {
        return addRow(List.of(cells));
    }

    public Table addRow(List<String> cells) {
        Function1<Integer, TableHeader> totalIndexToHeaderFn = headers.withDefaultValue(TableHeader.NON_EXISTING);

        List<TableCell> tableCells = cells
                .zipWithIndex()
                .map(valueToIndex -> valueToIndex.map2(idx -> totalIndexToHeaderFn
                        .apply(idx)
                        .isPrimaryKey()))
                .map(TableCell::create);


        TableRow row = TableRow.create(adjustCellsToHeaderCount(tableCells));
        return addRow(row);
    }

    public Table addRow(TableRow row) {
        return new Table(headers, rows.append(row));
    }

    public List<TableCell> adjustCellsToHeaderCount(List<TableCell> tableCells) {
        List<TableCell> shrinkedCells = tableCells.dropRight(tableCells.size() - headers.size());
        return shrinkedCells.padTo(headers.size(), TableCell.MISSING_CELL);
    }

    public Table normalize(List<TableHeader> targetHeaders) {
        Function1<TableHeader, Option<Integer>> totalCurrentHeaderPositionFn = targetHeaders
                .filter(headers::contains)
                .toMap(t -> Tuple.of(t, headers.indexOf(t)))
                .lift();

        Function1<TableRow, TableRow> reorderCellsFn = row -> targetHeaders
                .map(totalCurrentHeaderPositionFn)
                .map(optIndex -> optIndex.map(idx -> row.getCells().get(idx))
                        .getOrElse(() -> TableCell.MISSING_CELL))
                .foldLeft(TableRow.create(), TableRow::addCell);

        return rows.map(reorderCellsFn)
                .foldLeft(Table.create(targetHeaders), Table::addRow);
    }
}
