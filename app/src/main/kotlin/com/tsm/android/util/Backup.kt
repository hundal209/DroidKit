package com.tsm.android.util

import org.slf4j.Logger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

object Backup {
    /** Crates a backup of [src] at `$src.backup`. Return [src] for chaining */
    internal fun backup(src: Path, logger: Logger? = null): Path {
        // Do not backup a file that does not exist
        if (src.exists()) {
            val backup = findBackup(src)
            logger?.debug("Backing up {} at {}", src, backup)
            Files.copy(src, backup, StandardCopyOption.REPLACE_EXISTING)
        }
        return src
    }

    private fun findBackup(src: Path): Path {
        var backup = src.resolveSibling("$src.backup")

        // Start at 2 so suffix is backup2, backup3, etc.
        var i = 2
        while (Files.exists(backup)) {
            backup = src.resolveSibling("$src.backup")
            i++
        }

        return backup
    }
}