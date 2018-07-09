package com.github.dmn1k.table.diff;


import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.collection.List;
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
        return addRawRow(row);
    }

    /**
     * Reorders all columns according to targetHeaders and creates dummy columns if a header in targetHeaders is missing
     * @param targetHeaders defines the structure of the resulting table
     * @return a normalized table according to the given structure
     */
    public Table normalize(List<TableHeader> targetHeaders) {
        Function1<TableRow, TableRow> normalizeFn = row -> targetHeaders
                .map(headers::indexOfOption) // index of target header in current headers
                .map(row::getCellOrMissing) // cell with given index or MISSING_CELL
                .foldLeft(TableRow.create(), TableRow::addCell);

        return rows.map(normalizeFn)
                .foldLeft(Table.create(targetHeaders), Table::addRawRow);
    }

    private List<TableCell> adjustCellsToHeaderCount(List<TableCell> tableCells) {
        List<TableCell> shrinkedCells = tableCells.dropRight(tableCells.size() - headers.size());
        return shrinkedCells.padTo(headers.size(), TableCell.MISSING_CELL);
    }

    /**
     * Adds row without any adjustments like adding/dropping cells to adjust cell-count to header-count
     * @param row the row to add
     * @return an updated table
     */
    private Table addRawRow(TableRow row) {
        return new Table(headers, rows.append(row));
    }

}
