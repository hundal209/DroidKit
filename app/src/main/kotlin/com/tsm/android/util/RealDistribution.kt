package com.tsm.android.util
import java.io.FileNotFoundException
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name

interface Distribution {
    /** Find a catalog, which is stored in the dist root, at dist/[name]. */
    fun findCatalog(name: String): Path

    /** Find a nested file, located at dist/[root]/[name]. */
    fun findConfigurationFile(
        root: String,
        name: String
    ): Path
}

internal class RealDistribution private constructor(fs: FileSystem) : Distribution {

    private val pwd = fs.getPath(".").toAbsolutePath().normalize()
    private val distPath = "app/binary"

    override fun findCatalog(name: String): Path = find(name)
    override fun findConfigurationFile(
        root: String,
        name: String
    ): Path = find(root, name)

    private fun find(
        root: String,
        vararg others: String = emptyArray()
    ): Path {
        // This will be the path when sa-toolbox is run from the repo root
        pwd.resolve(distPath, root, *others).also {
            if (it.exists()) return it
        }

        // Fallback
        val filename = if (others.isNotEmpty()) others.last() else root
        return Files.walk(pwd).filter {
            it.name == filename
        }.findFirst().orElseThrow {
            FileNotFoundException("Cannot find $filename")
        }
    }

    companion object {
        fun of(fs: FileSystem): Distribution = RealDistribution(fs)
    }
}