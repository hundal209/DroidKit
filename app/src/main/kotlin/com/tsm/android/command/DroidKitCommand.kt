package com.tsm.android.command

import picocli.CommandLine.Command

/**
 * Parent command and entry point to any of the CLI usages of DroidKit
 */
@Command(
    name = "droid-kit",
    mixinStandardHelpOptions = true,
    version = ["1.0"],
    description = ["Android Env Dev Assistant"],
    subcommands = [

    ]
)
class DroidKitCommand