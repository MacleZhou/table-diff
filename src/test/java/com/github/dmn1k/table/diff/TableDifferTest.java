package com.github.dmn1k.table.diff;

import io.vavr.collection.List;
import io.vavr.control.Option;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;

class TableDifferTest {
    @DisplayName("in case of two symmetric tables (columns are the same)")
    @Nested
    class SymmetricCases {
        @Nested
        @DisplayName("returns results of type UNCHANGED")
        class UnchangedCases {
            @DisplayName("if both tables are exactly the same")
            @ParameterizedTest
            @ArgumentsSource(ArbitraryTablesProvider.class)
            void returnsUnchangedResultIfBothTablesAreExactlyTheSame(Table table) {
                TableDiffer tableDiffer = new TableDiffer();

                List<TableDiffResult> result = tableDiffer.diff(Option.of(table), Option.of(table));

                assertThat(result).hasSize(table.getRows().size());
                assertThat(result).allMatch(r -> DiffType.Unchanged.equals(r.getDiffType()));
            }

            @DisplayName("for each row which exactly matches between the two tables")
            @Test
            void returnsUnchangedResultIfRowsMatchExactly() {
                TableDiffer tableDiffer = new TableDiffer();
                Table header = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y")
                );

                List<TableDiffResult> result = tableDiffer.diff(
                        Option.of(header.addRow("a", "1")
                                .addRow("b", "2")
                                .addRow("c", "2")),
                        Option.of(header.addRow("a", "1")
                                .addRow("b", "3")
                                .addRow("c", "2"))
                );

                assertThat(result.get(0).getDiffType()).isEqualTo(DiffType.Unchanged);
                assertThat(result.get(2).getDiffType()).isEqualTo(DiffType.Unchanged);
            }

            @DisplayName("for unchanged rows matched by primary key")
            @Test
            void returnsUnchangedResultForUnchangedRowsMatchedByPrimaryKey() {
                TableDiffer tableDiffer = new TableDiffer();
                Table table = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y")
                );

                List<TableDiffResult> result = tableDiffer.diff(
                        Option.of(table.addRow("a", "1")
                                .addRow("b", "2")
                                .addRow("c", "2")),
                        Option.of(table.addRow("b", "1")
                                .addRow("c", "2")
                                .addRow("a", "1"))
                );

                assertThat(result).hasSize(3);
                assertThat(result.get(0).getDiffType()).isEqualTo(DiffType.Unchanged);
                assertThat(result.get(2).getDiffType()).isEqualTo(DiffType.Unchanged);
            }
        }

        @Nested
        @DisplayName("returns results of type NEW")
        class NewCases {
            @DisplayName("if there is no old table")
            @ParameterizedTest
            @ArgumentsSource(ArbitraryTablesProvider.class)
            void returnsNewResultForEachRowInNewTable(Table table) {
                TableDiffer tableDiffer = new TableDiffer();

                List<TableDiffResult> result = tableDiffer.diff(Option.of(table), Option.none());

                assertThat(result).hasSize(table.getRows().size());
                assertThat(result).allMatch(r -> DiffType.New.equals(r.getDiffType()));
            }

            @DisplayName("if part of composite primary key has changed")
            @Test
            void returnsChangedResultIfCellChangedIdentifiedByCompositeKey() {
                TableDiffer tableDiffer = new TableDiffer();
                Table header = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y"),
                        TableHeader.createPrimaryKey("z")
                );

                List<TableDiffResult> result = tableDiffer.diff(
                        Option.of(header
                                .addRow("a", "b", "c")
                        ),
                        Option.of(header
                                .addRow("a", "b", "d")
                        )
                );

                assertThat(result).hasSize(2);
                assertThat(result.get(0).getDiffType()).isEqualTo(DiffType.New);
                assertThat(result.get(1).getDiffType()).isNotEqualTo(DiffType.New);
            }
        }

        @Nested
        @DisplayName("returns results of type DELETED")
        class DeletedCases {
            @DisplayName("if there is no new table")
            @ParameterizedTest
            @ArgumentsSource(ArbitraryTablesProvider.class)
            void returnsDeletedResultForEachRowInOldTable(Table table) {
                TableDiffer tableDiffer = new TableDiffer();

                List<TableDiffResult> result = tableDiffer.diff(Option.none(), Option.of(table));

                assertThat(result).hasSize(table.getRows().size());
                assertThat(result).allMatch(r -> DiffType.Deleted.equals(r.getDiffType()));
            }

            @DisplayName("if part of composite primary key has changed")
            @Test
            void returnsChangedResultIfCellChangedIdentifiedByCompositeKey() {
                TableDiffer tableDiffer = new TableDiffer();
                Table header = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y"),
                        TableHeader.createPrimaryKey("z")
                );

                List<TableDiffResult> result = tableDiffer.diff(
                        Option.of(header
                                .addRow("a", "b", "c")
                        ),
                        Option.of(header
                                .addRow("a", "b", "d")
                        )
                );

                assertThat(result).hasSize(2);
                assertThat(result.get(0).getDiffType()).isNotEqualTo(DiffType.Deleted);
                assertThat(result.get(1).getDiffType()).isEqualTo(DiffType.Deleted);
            }

            @DisplayName("returns all cells of deleted rows")
            @Test
            void returnsAllCellsOfDeletedRows() {
                TableDiffer tableDiffer = new TableDiffer();
                Table header = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y"),
                        TableHeader.create("z")
                );

                List<TableDiffResult> result = tableDiffer.diff(
                        Option.none(),
                        Option.of(header
                                .addRow("a", "b", "d")
                        )
                );

                assertThat(result).hasSize(1);
                assertThat(result)
                        .extracting(r -> r.getOldRow().map(TableRow::getCells).getOrElse(List.empty()))
                        .allMatch(cells -> cells.size() == 3);
            }
        }

        @Nested
        @DisplayName("returns results of type CHANGED")
        class ChangedCases {
            @DisplayName("if there is a changed column in one of the rows")
            @Test
            void returnsChangedResultIfCellChanged() {
                TableDiffer tableDiffer = new TableDiffer();
                Table header = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y")
                );

                List<TableDiffResult> result = tableDiffer.diff(
                        Option.of(header.addRow("1", "2")),
                        Option.of(header.addRow("1", "3"))
                );

                assertThat(result).hasSize(1);
                assertThat(result.get(0).getDiffType()).isEqualTo(DiffType.Changed);
            }

            @DisplayName("if row identified by composite key has changed")
            @Test
            void returnsChangedResultIfCellChangedIdentifiedByCompositeKey() {
                TableDiffer tableDiffer = new TableDiffer();
                Table header = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y"),
                        TableHeader.createPrimaryKey("z")
                );

                List<TableDiffResult> result = tableDiffer.diff(
                        Option.of(header
                                .addRow("1", "2", "3")
                                .addRow("a", "b", "c")
                        ),
                        Option.of(header
                                .addRow("1", "3", "3")
                                .addRow("a", "b", "d")
                        )
                );

                assertThat(result).hasSize(3);
                assertThat(result.get(0).getDiffType()).isEqualTo(DiffType.Changed);
                assertThat(result.get(1).getDiffType()).isNotEqualTo(DiffType.Changed);
                assertThat(result.get(2).getDiffType()).isNotEqualTo(DiffType.Changed);
            }
        }


        @Test
        void returnsEmptyResultIfBothTablesAreEmpty() {
            TableDiffer tableDiffer = new TableDiffer();

            List<TableDiffResult> result = tableDiffer.diff(Option.none(), Option.none());

            assertThat(result).hasSize(0);
        }

        @Test
        void normalizesCellValueCountToMatchHeaders() {
            TableDiffer tableDiffer = new TableDiffer();
            Table header = Table.create(
                    TableHeader.createPrimaryKey("x"),
                    TableHeader.createPrimaryKey("y"),
                    TableHeader.create("z")
            );

            List<TableDiffResult> result = tableDiffer.diff(
                    Option.of(header
                            .addRow("1", "2", "3")
                            .addRow("a", "b", "c", "d")
                    ),
                    Option.of(header
                            .addRow("1", "2")
                            .addRow("a", "b", "d")
                    )
            );

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(TableDiffResult::getNewRow)
                    .extracting(r -> r.map(TableRow::getCells).getOrElse(List.empty()))
                    .allMatch(list -> list.size() == 3);
            assertThat(result)
                    .extracting(TableDiffResult::getOldRow)
                    .extracting(r -> r.map(TableRow::getCells).getOrElse(List.empty()))
                    .allMatch(list -> list.size() == 3);
        }
    }


    @DisplayName("in case of two asymmetric tables (columns are NOT the same)")
    @Nested
    class AsymmetricCases {
        @Nested
        @DisplayName("when columns are not in the same order")
        class ColumnShuffledCases {

            @DisplayName("columns are sorted and diff proceeds as normal")
            @Test
            void normalizesCellValueCountToMatchHeaders() {
                TableDiffer tableDiffer = new TableDiffer();
                Table header1 = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.createPrimaryKey("y"),
                        TableHeader.create("z")
                );
                Table header2 = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("z"),
                        TableHeader.createPrimaryKey("y")
                );


                List<TableDiffResult> result = tableDiffer.diff(
                        Option.of(header1
                                .addRow("1", "2", "3")
                                .addRow("a", "b", "c")
                        ),
                        Option.of(header2
                                .addRow("1", "3", "2")
                                .addRow("a", "c", "b")
                        )
                );

                assertThat(result).hasSize(2);
                assertThat(result).allMatch(r -> DiffType.Unchanged.equals(r.getDiffType()));
            }
        }

        @Nested
        @DisplayName("when old columns are removed")
        class ColumnsRemovedCases {

            @DisplayName("missing columns are ignored if IGNORE_ALL_MISSING_COLUMNS strategy is used")
            @Test
            void ignoresAdditionalColumsInOldTable() {
                TableDiffer tableDiffer = new TableDiffer()
                        .withColumnComparisonStrategy(ColumnComparisonStrategies.IGNORE_ALL_MISSING_COLUMNS);

                Table header1 = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y"),
                        TableHeader.create("z"),
                        TableHeader.create("z2")
                );

                Table header2 = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y"),
                        TableHeader.create("z"),
                        TableHeader.create("a")
                );


                List<TableDiffResult> result = tableDiffer.diff(
                        Option.of(header1
                                .addRow("1", "2", "A", "3")
                                .addRow("a", "b", "B", "c")
                        ),
                        Option.of(header2
                                .addRow("1", "2", "A", "3")
                                .addRow("a", "b", "B", "c")
                        )
                );

                assertThat(result).hasSize(2);
                assertThat(result).allMatch(r -> DiffType.Unchanged.equals(r.getDiffType()));
            }

            @DisplayName("missing columns in new table are ignored if IGNORE_MISSING_COLUMNS_IN_NEW_TABLE strategy is used")
            @Test
            void ignoresMissingCellsInNewTable() {
                TableDiffer tableDiffer = new TableDiffer()
                        .withColumnComparisonStrategy(ColumnComparisonStrategies.IGNORE_MISSING_COLUMNS_IN_NEW_TABLE);

                Table header1 = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y"),
                        TableHeader.create("z"),
                        TableHeader.create("z2")
                );

                Table header2 = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y"),
                        TableHeader.create("a"),
                        TableHeader.create("z")
                );


                List<TableDiffResult> result = tableDiffer.diff(
                        Option.of(header1
                                .addRow("1", "2", "A", "3")
                                .addRow("a", "b", "B", "c")
                        ),
                        Option.of(header2
                                .addRow("1", "2", "A", "3")
                                .addRow("a", "b", "B", "c")
                        )
                );

                assertThat(result).hasSize(2);
                assertThat(result).allMatch(r -> DiffType.Changed.equals(r.getDiffType()));
            }

            @DisplayName("missing columns in new table are ignored if IGNORE_MISSING_COLUMNS_IN_OLD_TABLE strategy is used")
            @Test
            void ignoresMissingCellsInOldTable() {
                TableDiffer tableDiffer = new TableDiffer()
                        .withColumnComparisonStrategy(ColumnComparisonStrategies.IGNORE_MISSING_COLUMNS_IN_OLD_TABLE);

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
                        Option.of(header1
                                .addRow("1", "2", "A", "3")
                                .addRow("a", "b", "B", "c")
                        ),
                        Option.of(header2
                                .addRow("1", "2", "3")
                                .addRow("a", "b", "c")
                        )
                );

                assertThat(result).hasSize(2);
                assertThat(result).allMatch(r -> DiffType.Unchanged.equals(r.getDiffType()));
            }

            @DisplayName("missing columns are considered as change by default")
            @Test
            void considersAdditionalColumnsInOldTableAsChange() {
                TableDiffer tableDiffer = new TableDiffer();
                Table header1 = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y")
                );

                Table header2 = Table.create(
                        TableHeader.createPrimaryKey("x"),
                        TableHeader.create("y"),
                        TableHeader.create("z")
                );


                List<TableDiffResult> result = tableDiffer.diff(
                        Option.of(header1
                                .addRow("1", "3")
                                .addRow("a", "c")
                        ),
                        Option.of(header2
                                .addRow("1", "A", "3")
                                .addRow("a", "B", "c")
                        )
                );

                assertThat(result).hasSize(2);
                assertThat(result).allMatch(r -> DiffType.Changed.equals(r.getDiffType()));
            }
        }

        @Nested
        @DisplayName("when new columns are added")
        class ColumnsAddedCases {
            @DisplayName("added columns are ignored if IGNORE_ALL_MISSING_COLUMNS strategy is used")
            @Test
            void ignoresAdditionalColumsInNewTable() {
                TableDiffer tableDiffer = new TableDiffer()
                        .withColumnComparisonStrategy(ColumnComparisonStrategies.IGNORE_ALL_MISSING_COLUMNS);
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
                        Option.of(header1
                                .addRow("1", "2", "A", "3")
                                .addRow("a", "b", "B", "c")
                        ),
                        Option.of(header2
                                .addRow("1", "2", "3")
                                .addRow("a", "b", "c")
                        )
                );

                assertThat(result).hasSize(2);
                assertThat(result).allMatch(r -> DiffType.Unchanged.equals(r.getDiffType()));
            }

            @DisplayName("added columns are considered a change by default")
            @Test
            void doesntIgnoreAdditionalColumnsByDefault() {
                TableDiffer tableDiffer = new TableDiffer();
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
                        Option.of(header1
                                .addRow("1", "2", "A", "3")
                                .addRow("a", "b", "B", "c")
                        ),
                        Option.of(header2
                                .addRow("1", "2", "3")
                                .addRow("a", "b", "c")
                        )
                );

                assertThat(result).hasSize(2);
                assertThat(result).allMatch(r -> DiffType.Changed.equals(r.getDiffType()));
            }
        }
    }

}
