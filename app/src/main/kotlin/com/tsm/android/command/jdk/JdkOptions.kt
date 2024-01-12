package com.tsm.android.command.jdk

import com.tsm.android.catalog.JdkCatalog.Companion.Jdk
import com.tsm.android.converter.JdkConverter
import picocli.CommandLine.Option

class JdkOptions {
    @Option(
        names = ["-j", "--jdk"],
        defaultValue = "azul-17",
        converter = [JdkConverter::class],
        description = [
            "See jdk-catalog.txt for valid values."
        ]
    )
    lateinit var jdk: Jdk
}