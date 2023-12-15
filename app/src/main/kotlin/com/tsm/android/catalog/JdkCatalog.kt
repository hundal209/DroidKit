package com.tsm.android.catalog

import com.tsm.android.util.Serialization.fromJson
import kotlinx.serialization.Serializable
import java.nio.file.Files.readString
import java.nio.file.Path

@Serializable
data class JdkCatalog(private val jdks: Map<String, Jdk>) {

    constructor(vararg jdks: Pair<String, Jdk>) : this(jdks = jdks.toMap())

    companion object {
        fun of(file: Path): JdkCatalog = readString(file).fromJson()

        @Serializable
        data class Jdk(
            val version: String,
            val versionFile: String,
            val archivePrefix: String,
            val architecture: String,
            val url: String,
            val sha256: String
        ) {
            val name = "$version-$architecture"
        }
    }

    fun find(version: String): Jdk? = jdks[version]
}