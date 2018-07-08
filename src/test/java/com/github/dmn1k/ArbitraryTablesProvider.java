package com.github.dmn1k;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class ArbitraryTablesProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
        return Stream.of(
                Table.create(TableHeader.create("x"), TableHeader.create("y"))
                        .addRow("1", "2"),
                Table.create(TableHeader.create("x"), TableHeader.create("y"))
                        .addRow("1", "2")
                        .addRow("3", "4"),
                Table.create(TableHeader.create("x"), TableHeader.create("y"), TableHeader.create("z"))
                        .addRow("1", "2", "3")
                        .addRow("3", "4", "5")
                        .addRow("5", "6", "7")
        ).map(Arguments::of);
    }
}
