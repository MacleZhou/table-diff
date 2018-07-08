package com.github.dmn1k;

import lombok.ToString;
import lombok.Value;

@Value
@ToString
public class TableDiffResult {
    private DiffType diffType;
}
