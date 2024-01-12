package com.tsm.android.util

import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import okio.buffer
import okio.sink
import org.slf4j.Logger
import java.io.IOException
import java.nio.file.Files.createDirectories
import java.nio.file.Files.exists
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

interface Download {
    fun get(
        url: String,
        sha256: String,
        bypassSha: Boolean = false,
        destination: Path,
        monitor: () -> ProgressBarBuilder?
    ): Path

    fun get(
        url: String,
        sha256: String,
        bypassSha: Boolean = false,
        monitor: () -> ProgressBarBuilder? = { null }
    ): Path
}

/**
 * Implementation type of Download that uses [OkHttp] as the downloading client
 */

internal class OkHttpDownloads(
    private val downloadDirectory: Path,
    private val logger: Logger,
    private val okHttp: OkHttpClient = OkHttpClient.Builder().build(),
    private val sha: SHA256 = SHA256()
) : Download {
    override fun get(
        url: String,
        sha256: String,
        bypassSha: Boolean,
        destination: Path,
        monitor: () -> ProgressBarBuilder?
    ): Path = get(
        request = Request.Builder()
            .url(url)
            .build(),
        sha256 = sha256,
        bypassSha = bypassSha,
        destination = destination,
        monitor = monitor
    )

    override fun get(url: String, sha256: String, bypassSha: Boolean, monitor: () -> ProgressBarBuilder?): Path {
        val request = Request.Builder()
            .url(url)
            .build()

        val destination = request.url.encodedPathSegments
            .fold(downloadDirectory) { d, seg -> d.resolve(seg) }
            .apply { createDirectories(parent) }

        return get(request, sha256, bypassSha, destination, monitor)
    }

    private fun get(
        request: Request,
        sha256: String,
        bypassSha: Boolean,
        destination: Path,
        monitor: () -> ProgressBarBuilder?
    ): Path {
        when {
            !exists(destination) -> {
                // do nothing
            }

            ((bypassSha && destination.exists()) || (!bypassSha && sha.check(sha256, destination, logger))) -> {
                logger.debug("{} has already been downloaded.", request.url)
                return destination
            }

            else -> {
                destination.deleteIfExists()
                logger.warn("${request.url} has already been downloaded, but corrupted. Re-downloading!")
            }
        }

        okHttp.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val body = response.body!!
            val source = body.source()

            reportTo(monitor(), body.contentLength()).use { progress ->
                val sink = destination.sink().buffer()
                val buffer = Buffer()
                var byteCount: Long
                while (source.read(buffer, 8192L).also { byteCount = it } != -1L) {
                    sink.write(buffer, byteCount)
                    sink.flush()
                    progress.report(byteCount)
                }
            }
        }
        if (!bypassSha && !sha.check(sha256, destination, logger)) {
            throw IOException("Download of ${request.url} is corrupted.")
        }
        return destination
    }

    private fun reportTo(
        bar: ProgressBarBuilder?,
        total: Long
    ): Progress {
        return when (bar) {
            null -> IgnoreProgress()
            else -> ReportProgress(
                bar.setInitialMax(total)
                    .setUnit("MiB", 1024L * 1024L)
                    .setMaxRenderedLength(ProgressBars.LENGTH)
                    .showSpeed()
                    .build()
            )
        }
    }

    private interface Progress : AutoCloseable {
        fun report(progress: Long)
    }

    private class ReportProgress(val progressBar: ProgressBar) : Progress, AutoCloseable by progressBar {
        override fun report(progress: Long) {
            progressBar.stepBy(progress)
        }
    }

    private class IgnoreProgress : Progress {
        override fun report(progress: Long) { }
        override fun close() { }
    }

}