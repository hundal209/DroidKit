package com.tsm.android.util

enum class Status {
    VALID,
    WARNING,
    ERROR;

    inline fun onError(doAction: () -> Unit) {
        if (this == ERROR) {
            doAction()
        }
    }

    operator fun plus(other: Status?): Status {
        return when {
            other == null -> this
            ordinal > other.ordinal -> this
            ordinal < other.ordinal -> other
            else -> this
        }
    }
}