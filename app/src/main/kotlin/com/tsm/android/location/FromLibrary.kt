package com.tsm.android.location

import java.nio.file.Path

/**
 * [FromLibrary] provides a path from `Library` path
 */
interface FromLibrary {
    val Location.library: Path get() = resolve("Library")
}