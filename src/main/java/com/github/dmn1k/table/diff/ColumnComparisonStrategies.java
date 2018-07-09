package com.github.dmn1k.table.diff;

import io.vavr.Function2;

import java.util.Objects;

public final class ColumnComparisonStrategies {
    private ColumnComparisonStrategies(){
        // static class
    }

    public static final Function2<TableCell, TableCell, Boolean> CONSIDER_MISSING_COLUMNS_AS_CHANGE =
            (a, b) -> !a.isMissing() && !b.isMissing() && Objects.equals(a.getValue(), b.getValue());

    public static final Function2<TableCell, TableCell, Boolean> IGNORE_ALL_MISSING_COLUMNS =
            (a, b) -> a.isMissing() || b.isMissing() || CONSIDER_MISSING_COLUMNS_AS_CHANGE.apply(a, b);

    public static final Function2<TableCell, TableCell, Boolean> IGNORE_MISSING_COLUMNS_IN_NEW_TABLE =
            (a, b) -> a.isMissing() || CONSIDER_MISSING_COLUMNS_AS_CHANGE.apply(a, b);

    public static final Function2<TableCell, TableCell, Boolean> IGNORE_MISSING_COLUMNS_IN_OLD_TABLE =
            (a, b) -> b.isMissing() || CONSIDER_MISSING_COLUMNS_AS_CHANGE.apply(a, b);


}
