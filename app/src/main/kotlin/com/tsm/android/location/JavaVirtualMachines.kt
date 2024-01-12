package com.tsm.android.location

import com.tsm.android.catalog.JdkCatalog
import java.nio.file.Path

class JavaVirtualMachines(override val directory: Path) : Location {

    companion object {
        fun GlobalScope.javaVirtualMachines() = userHome.javaVirtualMachines()

        fun UserHome.javaVirtualMachines() = JavaVirtualMachines(
            resolve("library", "Java", "JavaVirtualMachines")
        )
    }

    fun resolve(jdk: JdkCatalog.Companion.Jdk): Path = resolve(jdk.name)

    fun resolveJavaHome(jdk: JdkCatalog.Companion.Jdk): Path =
        resolve(jdk).resolve("Contents/Home")
}