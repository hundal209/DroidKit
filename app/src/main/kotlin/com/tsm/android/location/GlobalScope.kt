package com.tsm.android.location

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

/**
 * GlobalScope encapsulates the data from Filesystem and provides
 * a way to access inner
 */
data class GlobalScope(
    override val directory: Path,
    val user: String
): Location {

    val userHome by lazy { UserHome(resolve("Users", user)) }

    val fileSystem: FileSystem by lazy {
        directory.fileSystem
    }

    companion object {
        fun create(): GlobalScope {
            return GlobalScope(
                directory = FileSystems.getDefault().getPath("/"),
                user = System.getenv("USER")
            )
        }
    }
}