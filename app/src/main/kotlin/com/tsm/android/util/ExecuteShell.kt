package com.tsm.android.util

import org.slf4j.Logger
import java.nio.file.FileSystem
import java.nio.file.Path

class ExecuteShell(
    private val execute: Execute,
    private val logger: Logger,
    private val resolve: (CharSequence) -> Path
) : Shell {

    companion object {
        fun using(
            execute: Execute,
            fileSystem: FileSystem,
            logger: Logger
        ): ExecuteShell {
            return ExecuteShell(execute, logger) { path -> fileSystem.getPath(path.toString()) }
        }
    }

    override val type: String by lazy {
        System.getenv("SHELL")
    }

    override val rc: Path by lazy {
        val home = resolve(System.getenv("HOME"))
        when (type) {
            // Only support these shells, otherwise too many to maintain
            "/bin/zsh" -> home.resolve(".zshrc")
            "/bin/bash" -> home.resolve(".bashrc")
            "/bin/ksh" -> home.resolve(".kshrc")
            else -> error("unsupported shell: $type")
        }
    }

    override fun source(file: Path): Boolean {
        return shell("source", file.toString()).run {
            when (exit) {
                0 -> true
                else -> {
                    logger.warn("source $file failed, stderr:\n $err")
                    false
                }
            }
        }
    }

    override fun open(app: Path, environment: Map<String, String>) {
        execute.call(
            "open",
            *(
               environment.flatMap { (variable, value) ->
                listOf("--env", "$variable=$value")
            }.toTypedArray() + app.toString()
            ),
            workingDirectory = resolve(".")
        )
    }

    private fun shell(vararg commands: String): Execute.Result {
        return execute.call(
            type,
            "-c",
            "source $rc; ${commands.joinToString(" ")}",
            workingDirectory = resolve(".")
        )
    }
}