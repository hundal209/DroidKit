package com.tsm.android.command

import com.tsm.android.command.jdk.SetupJdkCommand
import picocli.CommandLine
import picocli.CommandLine.Command

@Command(
    name = "setup",
    mixinStandardHelpOptions = true,
    version = ["1.0"],
    description = ["Setup adk, jdk, or ide."],
    subcommands = [
        SetupJdkCommand::class,
        CommandLine.HelpCommand::class
    ]
)
class SetupCommand