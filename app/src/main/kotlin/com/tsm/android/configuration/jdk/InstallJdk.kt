package com.tsm.android.configuration.jdk

import com.tsm.android.catalog.JdkCatalog.Companion.Jdk
import com.tsm.android.configuration.Configuration
import com.tsm.android.location.JavaVirtualMachines
import com.tsm.android.util.*
import me.tongfei.progressbar.ProgressBarBuilder
import org.slf4j.Logger
import java.lang.Exception
import java.net.URI
import java.nio.file.Files
import java.nio.file.Files.createSymbolicLink
import java.nio.file.Files.exists
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.readText
import kotlin.streams.asSequence

/**
 * Install JDK at destination
 */
class InstallJdk(
    private val download: Download,
    private val archive: Archive,
    private val logger: Logger
) : Configuration<Jdk, JavaVirtualMachines> {
    override fun applyFor(chosen: Jdk, to: JavaVirtualMachines): Status {
        try {
            val destination = to.resolve(chosen)
            val jdkArchive = download.get(chosen.url, chosen.sha256) {
                ProgressBarBuilder().setTaskName("Downloading JDK from ${URI.create(chosen.url).host}")
            }

            val unzip = archive
                .unpack(
                    from = jdkArchive,
                    inDirectory = destination,
                    monitor = { ProgressBarBuilder().setTaskName("Unpacking jdk") },
                    upToDate = hasMatching(chosen.versionFile, inDirectory = destination),
                    mapName = { name ->
                        name.removePrefix(chosen.archivePrefix + "/")
                    }
                )
                .toList()

            // Some JDK nest the content directory. IntelliJ cannot handle this so we create
            // a symlink for Intellij/Android Studio
            val contents = destination.resolve("Contents")
            if (!exists(contents)) {
                val nestedContents = unzip.firstOrNull() { path -> path.endsWith("Contents") }
                    ?: error("Can't find contents directory in $unzip")
                createSymbolicLink(contents, nestedContents)
            }
        } catch (ex: Exception) {
            logger.error("Error installed ${chosen.name}: ${ex.message}")
            return Status.ERROR
        }
        return Status.VALID
    }
    private fun hasMatching(
        file: String,
        inDirectory: Path
    ): UpToDate {
        return UpToDate { files: Iterable<Entry> ->
            if (!exists(inDirectory)) {
                return@UpToDate false
            }
            val archived = files
                .firstOrNull { file in it.name }
                ?.run {
                    contents().readBytes().toString(Charsets.UTF_8).trim()
                }
                ?: error("Could not file $file in $files")

            val installed =
                Files.walk(inDirectory).asSequence()
                    .firstOrNull()
                    ?.readText(Charsets.UTF_8)
                    ?.trim()

            if (archived == installed) {
                return@UpToDate true
            } else {
                logger.info("JDK update available")
                logger.debug("build ids don't match[$archived, $installed]")
            }

            logger.info("Removing old jdk")
            Files.walk(inDirectory).collect(Collectors.toList()).sortedDescending().forEach(Files::deleteIfExists)
            return@UpToDate false
        }
    }
}