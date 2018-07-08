package com.github.dmn1k;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
public class TableHeader {
    public static final TableHeader NON_EXISTING = TableHeader.create("__non_existing__");

    private String value;
    private boolean isPrimaryKey;

    public static TableHeader create(String value){
        return new TableHeader(value, false);
    }

    public static TableHeader createPrimaryKey(String value){
        return new TableHeader(value, true);
    }
}
