package com.github.dmn1k.table.diff;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
public class TableDiffer {
    private Function2<TableCell, TableCell, Boolean> cellComparisonFn = ColumnComparisonStrategies.CONSIDER_MISSING_COLUMNS_AS_CHANGE;

    /**
     * This strategy defines if and when two cells are considered equal.
     * Can be used to alter the diff-behavior
     * @param cellComparisonFn comparison fn
     * @return a new copy of this instance with the new comparison-fn
     */
    public TableDiffer withColumnComparisonStrategy(Function2<TableCell, TableCell, Boolean> cellComparisonFn){
        return new TableDiffer(cellComparisonFn);
    }

    /**
     * Compares two tables and creates a List of Diff-Results
     * @param newTable The new table (can be null)
     * @param oldTable The old table (can be null)
     * @return a list of diff-results
     */
    public List<TableDiffResult> diff(Table newTable, Table oldTable) {
        return diff(Option.of(newTable), Option.of(oldTable));
    }

    /**
     * Compares two tables and creates a List of Diff-Results
     * @param newTable The new table
     * @param oldTable The old table
     * @return a list of diff-results
     */
    public List<TableDiffResult> diff(Optional<Table> newTable, Optional<Table> oldTable) {
        return diff(Option.ofOptional(newTable), Option.ofOptional(oldTable));
    }

    /**
     * Compares two tables and creates a List of Diff-Results
     * @param newTable The new table
     * @param oldTable The old table
     * @return a list of diff-results
     */
    public List<TableDiffResult> diff(Option<Table> newTable, Option<Table> oldTable) {
        List<TableHeader> headerSuperset = createHeaderSuperset(newTable, oldTable);

        List<TableRow> newTableRows = toNormalizedRows(newTable, headerSuperset);
        List<TableRow> oldTableRows = toNormalizedRows(oldTable, headerSuperset);

        List<String> allPrimaryKeys = extractAllPrimaryKeys(newTableRows, oldTableRows);

        Function1<String, Tuple2<Option<TableRow>, Option<TableRow>>> findRowsByPrimaryKey = primKey -> Tuple.of(
                newTableRows.find(r -> r.primaryKeyValue().equals(primKey)),
                oldTableRows.find(r -> r.primaryKeyValue().equals(primKey))
        );

        return allPrimaryKeys
                .map(findRowsByPrimaryKey)
                .map(tuple -> TableDiffResult.create(tuple, cellComparisonFn))
                .sortBy(TableDiffResult::getPrimaryKey);
    }

    public List<String> extractAllPrimaryKeys(List<TableRow> newTableRows, List<TableRow> oldTableRows) {
        return newTableRows
                .appendAll(oldTableRows)
                .map(TableRow::primaryKeyValue)
                .distinct();
    }

    public List<TableHeader> createHeaderSuperset(Option<Table> newTable, Option<Table> oldTable) {
        return newTable
                .map(Table::getHeaders)
                .getOrElse(List.empty())
                .appendAll(oldTable.map(Table::getHeaders).getOrElse(List.empty()))
                .distinct();
    }

    private static List<TableRow> toNormalizedRows(Option<Table> table, List<TableHeader> headerSuperset) {
        return table
                    .map(t -> t.normalize(headerSuperset))
                    .map(Table::getRows)
                    .getOrElse(List.empty());
    }

}
