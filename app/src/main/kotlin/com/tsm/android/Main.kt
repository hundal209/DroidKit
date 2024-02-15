package com.tsm.android

import com.tsm.android.command.CommandLineHolder.Companion.newCommandLineHolder
import com.tsm.android.location.GlobalScope
import com.tsm.android.util.CatalogLocator
import com.tsm.android.util.ProcessExecute
import com.tsm.android.util.RealDistribution
import org.slf4j.LoggerFactory
import picocli.CommandLine

private val root = GlobalScope.create()
private val LOGGER = LoggerFactory.getLogger("droid-kit")

fun main(vararg args: String) {
    LOGGER.info("Welcome to droid-kit!")

    ProcessExecute(LOGGER).use { executor ->
        val distribution = RealDistribution.of(root.fileSystem)
        val catalogLocator = CatalogLocator(distribution)
        val holder = newCommandLineHolder(root, distribution, catalogLocator, executor, LOGGER)
        val cli = CommandLine(holder.command, holder.converterFactory)
        cli.isCaseInsensitiveEnumValuesAllowed = true
        cli.execute(*args)
    }
}