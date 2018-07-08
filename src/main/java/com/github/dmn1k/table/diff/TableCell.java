package com.github.dmn1k.table.diff;

import io.vavr.Tuple2;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(of = {"value"})
public class TableCell {
    public static final TableCell MISSING_CELL = new TableCell("", false, true);

    private String value;
    private boolean isPrimaryKey;
    private boolean isMissing;

    public static TableCell create(Tuple2<String, Boolean> valuePrimKeyTuple){
        return new TableCell(valuePrimKeyTuple._1, valuePrimKeyTuple._2, false);
    }
}
