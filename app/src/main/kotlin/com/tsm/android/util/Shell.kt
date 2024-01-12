package com.tsm.android.util

import java.nio.file.Path

/**
 * Shell abstraction over command line of user shell
 */
interface Shell {
    val type: CharSequence
    val rc: Path

    fun source(file: Path): Boolean

    fun open(app: Path, environment: Map<String, String>)
}