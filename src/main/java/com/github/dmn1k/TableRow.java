package com.github.dmn1k;

import io.vavr.collection.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static io.vavr.API.*;
import static io.vavr.Patterns.$Nil;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class TableRow {
    private List<TableCell> cells;

    public static TableRow create(List<TableCell> cells) {
        return new TableRow(cells);

    }

    public String primaryKeyValue() {
        List<TableCell> primKeyCells = cells.filter(TableCell::isPrimaryKey);

        return Match(primKeyCells).of(
                Case($Nil(), () -> cells),
                Case($(), () -> primKeyCells)
        )
                .map(TableCell::getValue)
                .foldLeft("", String::concat);

    }
}
