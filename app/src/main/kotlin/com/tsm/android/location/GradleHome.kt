package com.tsm.android.location

import java.nio.file.Path

/**
 * GradleHome directory from [UserHome]
 */
class GradleHome(override val directory: Path) : Location {
    companion object {
        fun UserHome.gradleHome() = GradleHome(resolve(".gradle"))
    }

    val properties = resolve("gradle.properties")
}