package com.tsm.android.util

import java.nio.file.Path

internal fun Path.resolve(other: String, vararg others: String): Path {
    return others.fold(resolve(other), Path::resolve)
}