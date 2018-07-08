package com.github.dmn1k;

import io.vavr.Function2;
import io.vavr.collection.List;
import lombok.*;

import static io.vavr.API.*;
import static io.vavr.Patterns.$Nil;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class TableRow {
    private List<TableCell> cells;

    public static TableRow create() {
        return create(List.empty());
    }

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

    public TableRow addCell(TableCell cell){
        return create(cells.append(cell));
    }

    public boolean isSameAs(TableRow normalizedOther, Function2<TableCell, TableCell, Boolean> comparisonFn) {
        if (cells.size() != normalizedOther.getCells().size()) {
            return false;
        }

        return cells.zipWith(normalizedOther.getCells(), comparisonFn)
                .foldLeft(true, (c1, c2) -> c1 && c2);
    }
}
