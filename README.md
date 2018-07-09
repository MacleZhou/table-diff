# table-diff

Easily diff tables in Java 8+:

```Java
TableDiffer tableDiffer = new TableDiffer()
                          .withColumnComparisonStrategy(IGNORE_ALL_MISSING_COLUMNS);
                              
Table header1 = Table.create(
        TableHeader.createPrimaryKey("x"),
        TableHeader.create("y"),
        TableHeader.create("a"),
        TableHeader.create("z")
);

Table header2 = Table.create(
        TableHeader.createPrimaryKey("x"),
        TableHeader.create("y"),
        TableHeader.create("z")
);



List<TableDiffResult> result = tableDiffer.diff(
        header1
          .addRow("1", "2", "A", "3")
          .addRow("a", "b", "B", "c"),
        header2
          .addRow("1", "2", "3")
          .addRow("a", "b", "c")
);
```

Handles missing/added cells via multiple "ColumnComparisonStrategies":

* IGNORE_ALL_MISSING_COLUMNS: Ignores missing columns in new and old table
* IGNORE_MISSING_COLUMNS_IN_NEW_TABLE: Ignores missing columns in new table
* IGNORE_MISSING_COLUMNS_IN_OLD_TABLE: Ignores missing columns in old table
* CONSIDER_MISSING_COLUMNS_AS_CHANGE: Doesnt ignore missing columns at all

### TODO
* Include information on what exactly changed in a row
* Publish to maven central
