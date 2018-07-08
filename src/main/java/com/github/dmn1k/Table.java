package com.github.dmn1k;


import io.vavr.collection.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Table {
    private List<TableRow> rows = List.empty();

    public static Table create(String... headers) {
        return new Table();
    }

    public Table addRow(String... cells) {
        return new Table(rows.append(TableRow.create(cells)));
    }

    public List<TableRow> rows() {
        return rows;
    }
}
