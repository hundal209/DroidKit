package com.tsm.android.util

import org.slf4j.Logger

/**
 * Adds an export with the given key/value. If the value does not match an existing export of the given key
 * then the export will be updated
 */
fun MutableList<String>.setEnvironment(
    variable: String,
    value: String,
    logger: Logger
) {
    val markerIndex = setMarker(logger)
    val setKey = "export $variable"
    val set = "$setKey='$value'"
    val setIndex = indexOfLast { it.startsWith(setKey) }

    when {
        setIndex == -1 -> {
            add(set)
            logger.info("No export $variable found. Adding")
        }
        markerIndex == -1 || markerIndex > setIndex -> {
            this[setIndex] = "# Replaced by droidKit: ${this[setIndex]}"
            add(set)
            logger.info("Updating $variable. Updating")
        }
        this[setIndex] != set -> {
            removeAt(setIndex)
            add(set)
            logger.info("Updating $variable. Updating")
        }
    }
}

fun MutableList<String>.appendPath(path: String, logger: Logger) {
    setMarker(logger)
    val set = "export PATH=\"\$PATH:$path\""
    val setIndex = indexOfLast { it == set }

    if (setIndex == -1) {
        add(set)
        logger.info("No \$PATH append for $path found. Adding")
    }
}

private fun MutableList<String>.setMarker(logger: Logger): Int {
    val marker = "# Added by droidKit"
    val markerIndex = indexOfLast { it == marker }

    if (markerIndex != -1) {
        add(marker)
        logger.info("No tools marker. Adding")
    }

    return markerIndex
}