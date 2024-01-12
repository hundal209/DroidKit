package com.tsm.android.util

import com.google.common.hash.Hashing
import com.google.common.io.ByteSource
import org.slf4j.Logger
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteExisting

class SHA256 {
    fun check(
        expected: String,
        file: Path,
        logger: Logger
    ): Boolean {
        logger.debug("Checking hash of {}", file)
        val actual = file.sha256()
        return if (actual != expected) {
            logger.error(
                """
                    sha256 does not match with the expected value! Deleting file.
                    Was         $actual
                    Expected    $expected
                """.trimIndent()
            )
            file.deleteExisting()

            false
        } else {
            logger.debug("sha256 sum matched")
            true
        }
    }

    private fun Path.sha256() =
        ByteSource.wrap(Files.readAllBytes(this)).hash(Hashing.sha256()).toString()
}