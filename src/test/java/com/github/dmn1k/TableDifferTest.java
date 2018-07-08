package com.github.dmn1k;

import io.vavr.collection.List;
import io.vavr.control.Option;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;

class TableDifferTest {
    @Nested
    @DisplayName("returns results of type UNCHANGED")
    class UnchangedCases {
        @DisplayName("if both tables are exactly the same")
        @ParameterizedTest
        @ArgumentsSource(ArbitraryTablesProvider.class)
        void returnsUnchangedResultIfBothTablesAreExactlyTheSame(Table table) {
            TableDiffer tableDiffer = new TableDiffer();

            List<TableDiffResult> result = tableDiffer.diff(Option.of(table), Option.of(table));

            assertThat(result).hasSize(table.rows().size());
            assertThat(result).allMatch(r -> DiffType.Unchanged.equals(r.getDiffType()));
        }

        @DisplayName("for each row which exactly matches between the two tables")
        @Test
        void returnsChangedResultIfCellChanged() {
            TableDiffer tableDiffer = new TableDiffer();
            Table header = Table.create("x", "y");

            List<TableDiffResult> result = tableDiffer.diff(
                    Option.of(header.addRow("1", "1")
                            .addRow("1", "2")
                            .addRow("2", "2")),
                    Option.of(header.addRow("1", "1")
                            .addRow("1", "3")
                            .addRow("2", "2"))
            );

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

            assertThat(result).hasSize(table.rows().size());
            assertThat(result).allMatch(r -> DiffType.New.equals(r.getDiffType()));
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

            assertThat(result).hasSize(table.rows().size());
            assertThat(result).allMatch(r -> DiffType.Deleted.equals(r.getDiffType()));
        }
    }

    @Nested
    @DisplayName("returns results of type CHANGED")
    class ChangedCases {
        @DisplayName("if there is a changed column in one of the rows")
        @Test
        void returnsChangedResultIfCellChanged() {
            TableDiffer tableDiffer = new TableDiffer();
            Table header = Table.create("x", "y");

            List<TableDiffResult> result = tableDiffer.diff(
                    Option.of(header.addRow("1", "2")),
                    Option.of(header.addRow("1", "3"))
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDiffType()).isEqualTo(DiffType.Changed);
        }
    }


    @Test
    void returnsEmptyResultIfBothTablesAreEmpty() {
        TableDiffer tableDiffer = new TableDiffer();

        List<TableDiffResult> result = tableDiffer.diff(Option.none(), Option.none());

        assertThat(result).hasSize(0);
    }

}
