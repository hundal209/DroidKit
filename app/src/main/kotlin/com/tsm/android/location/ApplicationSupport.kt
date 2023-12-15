package com.tsm.android.location

import com.tsm.android.util.resolve
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * Application Support is reference the app support directory on the
 * filesystem
 */
class ApplicationSupport(override val directory: Path) : Location {
    companion object : FromLibrary {
        fun UserHome.applicationSupport() = ApplicationSupport(library.resolve("Application Support"))
    }

    val droidKit by lazy {  TsmAndroid(directory.resolve("TSM", "DroidKit").createDirectories()) }
}