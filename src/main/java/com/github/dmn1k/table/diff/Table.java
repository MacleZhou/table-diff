package com.github.dmn1k.table.diff;


import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
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

    /**
     * Adds a row to the table.
     * Adjusts the row to the given headers (adds dummy-cells or removes cells if necessary)
     *
     * @param cells The cells of the row to add
     * @return a new copy of this table with the added row
     */
    public Table addRow(String... cells) {
        return addRow(List.of(cells));
    }

    /**
     * Adds a row to the table.
     * Adjusts the row to the given headers (adds dummy-cells or removes cells if necessary)
     *
     * @param cells The cells of the row to add
     * @return a new copy of this table with the added row
     */
    public Table addRow(List<String> cells) {
        List<TableCell> tableCells = cells
                .zipWith(headers, (cell, header) -> Tuple.of(cell, header.isPrimaryKey()))
                .map(TableCell::create);

        TableRow row = TableRow.create(adjustCellsToHeaderCount(tableCells));
        return addRowWithoutNormalization(row);
    }

    /**
     * Reorders all columns according to targetHeaders and creates dummy columns if a header in targetHeaders is missing
     * @param targetHeaders defines the structure of the resulting table
     * @return a normalized table according to the given structure
     */
    public Table normalize(List<TableHeader> targetHeaders) {
        Function2<TableRow, Option<Integer>, TableCell> findInRowFn = (row, idx) -> idx
                .flatMap(row::getCell)
                .getOrElse(TableCell.MISSING_CELL);

        Function1<TableRow, TableRow> normalizeFn = row -> targetHeaders
                .map(headers::indexOfOption)
                .map(findInRowFn.curried().apply(row))
                .foldLeft(TableRow.create(), TableRow::addCell);

        return rows.map(normalizeFn)
                .foldLeft(Table.create(targetHeaders), Table::addRowWithoutNormalization);
    }

    private List<TableCell> adjustCellsToHeaderCount(List<TableCell> tableCells) {
        List<TableCell> shrinkedCells = tableCells.dropRight(tableCells.size() - headers.size());
        return shrinkedCells.padTo(headers.size(), TableCell.MISSING_CELL);
    }

    private Table addRowWithoutNormalization(TableRow row) {
        return new Table(headers, rows.append(row));
    }

}
