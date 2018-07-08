package com.github.dmn1k;


import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.collection.Map;
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
        return shrinkedCells.padTo(headers.size(), TableCell.PAD_CELL);
    }

    public Table normalize(List<TableHeader> target) {
        Function1<TableHeader, Integer> totalCurrentHeaderPositionFn = target
                .filter(headers::contains)
                .toMap(t -> Tuple.of(t, headers.indexOf(t)))
                .withDefaultValue(null);

        Function1<TableRow, TableRow> reorderCellsFn = row -> target
                .map(targetHeader ->
                        row.getCells().get(totalCurrentHeaderPositionFn.apply(targetHeader)))
                .foldLeft(TableRow.create(), TableRow::addCell);

        return rows.map(reorderCellsFn)
                .foldLeft(Table.create(target), Table::addRow);
    }
}
