package com.github.dmn1k;

import io.vavr.collection.List;
import io.vavr.control.Option;

public class TableDiffer {
    public List<TableDiffResult> diff(Option<Table> newTable, Option<Table> oldTable) {
        if (newTable.isEmpty()) {
            return oldTable
                    .map(t -> t.rows().map(r -> new TableDiffResult(DiffType.Deleted)))
                    .getOrElse(List::empty);
        } else if (oldTable.isEmpty()) {
            return newTable
                    .map(t -> t.rows().map(r -> new TableDiffResult(DiffType.New)))
                    .getOrElse(List::empty);
        }

        return newTable.get()
                .rows()
                .zipWith(oldTable.get().rows(), (r1, r2) -> r1.equals(r2)
                        ? new TableDiffResult(DiffType.Unchanged)
                        : new TableDiffResult(DiffType.Changed));
    }
}
