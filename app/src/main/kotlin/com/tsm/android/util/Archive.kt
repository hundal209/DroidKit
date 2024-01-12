package com.tsm.android.util

import com.tsm.android.util.PermissionUtils.permissionsFromMode
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import okio.buffer
import okio.sink
import okio.source
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.slf4j.Logger
import java.io.InputStream
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors.toList

data class Entry(
    val name: String,
    val contents: () -> InputStream
)

/** Allows checking and skipping an archive that is already up-to-date */
fun interface UpToDate {
    fun check(files: Iterable<Entry>): Boolean
}

interface Archive {
    fun unpack(
        from: Path,
        inDirectory: Path,
        monitor: () -> ProgressBarBuilder? = { null },
        upToDate: UpToDate? = null,
        filter: (Entry) -> Boolean = { true },
        mapName: (String) -> String = { it }
    ): Iterable<Path>
}

internal class ArchiveImpl(
    private val logger: Logger
) : Archive {
    override fun unpack(
        from: Path,
        inDirectory: Path,
        monitor: () -> ProgressBarBuilder?,
        upToDate: UpToDate?,
        filter: (Entry) -> Boolean,
        mapName: (String) -> String
    ): Iterable<Path> {
        return ZipFile(newByteChannel(from)).use { zipFile ->
            if (zipFile.check(upToDate)) {
                // return a list of paths for the current directory
                return@use walk(inDirectory).collect(toList())
            }

            zipFile.entries.toList()
                .let { entries ->
                    monitor()
                        ?.let { ProgressBar.wrap(entries, it) }
                        ?: entries
                }
                .filter { entry -> filter(zipFile.asEntry(entry)) }
                .mapNotNull { entry ->
                    inDirectory
                        .resolve(mapName(entry.name))
                        .also { entryDestination ->
                            check(entryDestination.startsWith(inDirectory)) {
                                "Cannot unpack $entryDestination, as it's outside of $inDirectory"
                            }
                            createDirectories(entryDestination.parent)
                            when {
                                // entry is same as root after mapName
                                entryDestination == inDirectory -> {}
                                entry.isDirectory -> {
                                    createDirectories(entryDestination)
                                    // Directory has no permissions, 16384 converted from decimal to octal is 40000
                                    if (entry.unixMode == 0 || entry.unixMode == 16384) {
                                        // Set to 16877, which is octal for 755
                                        setPosixFilePermissions(entryDestination, permissionsFromMode(16877))
                                    } else {
                                        setPosixFilePermissions(
                                            entryDestination,
                                            permissionsFromMode(entry.unixMode)
                                        )
                                    }
                                }

                                entry.isUnixSymlink -> {
                                    val target = zipFile.getInputStream(entry).source().buffer().readUtf8()
                                    deleteIfExists(entryDestination)
                                    createSymbolicLink(entryDestination, Path.of(target))
                                }

                                else -> {
                                    zipFile.getInputStream(entry)
                                        .source()
                                        .buffer()
                                        .readAll(
                                            newOutputStream(
                                                entryDestination,
                                                StandardOpenOption.CREATE,
                                                StandardOpenOption.TRUNCATE_EXISTING
                                            ).sink()
                                        )
                                    // File has no permissions, 32768 converted from decimal to octal is 100000
                                    if (entry.unixMode == 0 || entry.unixMode == 32768) {
                                        // Set to 33204, which is octal for 644
                                        setPosixFilePermissions(entryDestination, permissionsFromMode(33204))
                                    } else {
                                        setPosixFilePermissions(
                                            entryDestination,
                                            permissionsFromMode(entry.unixMode)
                                        )
                                    }
                                }
                            }
                        }
                }
        }
    }

    private fun ZipFile.check(upToDate: UpToDate?): Boolean {
        upToDate ?: return false
        return entries.toList().map {asEntry(it) }.toList().run(upToDate::check)
    }

    private fun ZipFile.asEntry(e: ZipArchiveEntry): Entry {
        return Entry(e.name) { getInputStream(e) }
    }
}