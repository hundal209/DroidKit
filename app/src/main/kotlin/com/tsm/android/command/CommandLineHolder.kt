package com.tsm.android.command

import com.tsm.android.di.PicoFactory
import com.tsm.android.location.GlobalScope
import com.tsm.android.util.CatalogLocator
import com.tsm.android.util.Distribution
import com.tsm.android.util.Execute
import org.slf4j.Logger

internal class CommandLineHolder(
    val command: DroidKitCommand,
    val converterFactory: PicoFactory
) {
    companion object {
        fun newCommandLineHolder(
            globalScope: GlobalScope,
            distribution: Distribution,
            catalogLocator: CatalogLocator,
            execute: Execute,
            logger: Logger
        ): CommandLineHolder {
            return CommandLineHolder(
                DroidKitCommand(),
                PicoFactory(
                    globalScope,
                    execute,
                    logger,
                    distribution,
                    catalogLocator
                )
            )
        }
    }
}