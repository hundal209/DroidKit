package com.tsm.android.location

import com.tsm.android.util.resolve
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

/**
 * A location is a known file system [Location] containing information
 * about configuration or installation
 */
interface Location {
    val directory: Path

    fun resolve(
        other: String,
        vararg others: String
    ): Path = directory.resolve(other, *others)

    fun exists(): Boolean = directory.exists()

    fun create(): Path = directory.createDirectories()
}