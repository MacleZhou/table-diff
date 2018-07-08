package com.github.dmn1k;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class TableDiffer {
    private Function2<TableCell, TableCell, Boolean> cellComparisonFn = CellComparisonStrategies.CONSIDER_MISSING_CELLS_AS_CHANGE;

    /**
     * This strategy defines if and when two cells are considered equal.
     * Can be used to alter the diff-behavior
     * @param cellComparisonFn comparison fn
     * @return a new copy of this instance with the new comparison-fn
     */
    public TableDiffer withCellComparisonStrategy(Function2<TableCell, TableCell, Boolean> cellComparisonFn){
        return new TableDiffer(cellComparisonFn);
    }

    /**
     * Compares two tables and creates a List of Diff-Results
     * @param newTable The new table
     * @param oldTable The old table
     * @return a list of diff-results
     */
    public List<TableDiffResult> diff(Option<Table> newTable, Option<Table> oldTable) {
        List<TableHeader> headerSuperset = newTable
                .map(Table::getHeaders)
                .getOrElse(List.empty())
                .appendAll(oldTable.map(Table::getHeaders).getOrElse(List.empty()))
                .distinct();

        List<TableRow> newTableRows = toTableRows(newTable, headerSuperset);
        List<TableRow> oldTableRows = toTableRows(oldTable, headerSuperset);

        List<String> allPrimaryKeys = newTableRows
                .appendAll(oldTableRows)
                .map(TableRow::primaryKeyValue)
                .distinct();

        Function1<String, Tuple2<Option<TableRow>, Option<TableRow>>> findRowsByPrimaryKey = primKey -> Tuple.of(
                newTableRows.find(r -> r.primaryKeyValue().equals(primKey)),
                oldTableRows.find(r -> r.primaryKeyValue().equals(primKey))
        );

        return allPrimaryKeys
                .map(findRowsByPrimaryKey)
                .map(tuple -> TableDiffResult.create(tuple, cellComparisonFn))
                .sortBy(TableDiffResult::getPrimaryKey);
    }

    private static List<TableRow> toTableRows(Option<Table> table, List<TableHeader> headerSuperset) {
        return table
                    .map(t -> t.normalize(headerSuperset))
                    .map(Table::getRows)
                    .getOrElse(List.empty());
    }

}
