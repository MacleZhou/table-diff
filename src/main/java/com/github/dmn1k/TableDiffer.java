package com.github.dmn1k;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.control.Option;

import java.util.function.Function;

public class TableDiffer {
    public List<TableDiffResult> diff(Option<Table> newTable, Option<Table> oldTable) {
        List<TableRow> newTableRows = newTable
                .map(Table::getRows)
                .getOrElse(List.empty());
        List<TableRow> oldTableRows = oldTable
                .map(Table::getRows)
                .getOrElse(List.empty());

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
                .map(TableDiffResult::create)
                .sortBy(TableDiffResult::getPrimaryKey);
    }

}
