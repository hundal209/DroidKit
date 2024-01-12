package com.tsm.android.command.jdk

import com.tsm.android.catalog.JdkCatalog.Companion.Jdk
import com.tsm.android.configuration.Configuration
import com.tsm.android.location.GlobalScope
import org.slf4j.Logger
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand
import picocli.CommandLine.Mixin
import java.util.concurrent.Callable

@Command(
    name = "jdk",
    description = ["Download, install, and set up the JDK."],
    subcommands = [
        HelpCommand::class
    ]
)
class SetupJdkCommand(
    private val globalScope: GlobalScope,
    private val logger: Logger,
    private val jdkConfiguration: Configuration<Jdk, GlobalScope>
) : Callable<Int> {

    @Mixin
    val jdk = JdkOptions()

    override fun call(): Int {
        logger.info("Setting up ${jdk.jdk.name}")

        val result = jdkConfiguration.applyFor(
            chosen = jdk.jdk,
            to = globalScope
        )

        result.onError {
            return ERROR
        }

        return SUCCESS
    }

    companion object {
        private const val ERROR = 1
        private const val SUCCESS = 0
    }

}