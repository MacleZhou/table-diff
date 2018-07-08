package com.github.dmn1k;

import io.vavr.Function2;

import java.util.Objects;

public final class CellComparisonStrategies {
    private CellComparisonStrategies(){
        // static class
    }

    public static final Function2<TableCell, TableCell, Boolean> IGNORE_ALL_MISSING_CELLS =
            (a, b) -> a.isMissing() || b.isMissing() || Objects.equals(a.getValue(), b.getValue());

    public static final Function2<TableCell, TableCell, Boolean> IGNORE_MISSING_CELLS_IN_NEW_TABLE =
            (a, b) -> a.isMissing() || Objects.equals(a.getValue(), b.getValue());

    public static final Function2<TableCell, TableCell, Boolean> IGNORE_MISSING_CELLS_IN_OLD_TABLE =
            (a, b) -> b.isMissing() || Objects.equals(a.getValue(), b.getValue());


    public static final Function2<TableCell, TableCell, Boolean> CONSIDER_MISSING_CELLS_AS_CHANGE =
            (a, b) -> Objects.equals(a.getValue(), b.getValue());
}
