package com.tsm.android.configuration.jdk

import com.tsm.android.catalog.JdkCatalog.Companion.Jdk
import com.tsm.android.configuration.Configuration
import com.tsm.android.location.JavaVirtualMachines
import com.tsm.android.location.UserHome
import com.tsm.android.util.Backup.backup
import com.tsm.android.util.Shell
import com.tsm.android.util.Status
import com.tsm.android.util.setEnvironment
import org.slf4j.Logger
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Files.write
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class ConfigureEnvironmentVariables(
    private val getRc: () -> Path,
    private val javaVirtualMachines: JavaVirtualMachines,
    private val source: (Path) -> Boolean,
    private val logger: Logger
) : Configuration<Jdk, UserHome> {

    private val rc by lazy { getRc() }

    companion object {
        fun from(
            shell: Shell,
            javaVirtualMachines: JavaVirtualMachines,
            logger: Logger
        ) = ConfigureEnvironmentVariables(
            { shell.rc },
            javaVirtualMachines,
            shell::source,
            logger
        )
    }
    override fun applyFor(chosen: Jdk, to: UserHome): Status {
        return try {
            val javaHome = javaVirtualMachines.resolveJavaHome(chosen)
            when {
                !Files.exists(rc) -> error("Unable to determine shell run commands. Tried $rc")
                !rc.setEnvironment(javaHome) -> error("Unable to configure $rc properly")
                else -> { }
            }
            Status.VALID
        } catch (ex: Exception) {
            logger.warn("Error while configuring shell: ${ex.message}")
            Status.WARNING
        }
    }

    private fun Path.setEnvironment(javaHome: Path): Boolean {
        val lines = Files.readAllLines(javaHome)
        val changedLines = lines.toMutableList()

        changedLines.run {
            setEnvironment(
                "JAVA_HOME",
                javaHome.toString(),
                logger
            )

            setEnvironment(
                "DROID_KIT_JAVA_HOME",
                javaHome.toString(),
                logger
            )

            setEnvironment(
                "STUDIO_GRADLE_JDK",
                javaHome.toString(),
                logger
            )
        }

        if (lines == changedLines) {
            return true
        }

        write(backup(this, logger), changedLines, Charsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)
        return source(this)
    }
}