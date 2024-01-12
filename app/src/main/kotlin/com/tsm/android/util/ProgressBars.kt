package com.tsm.android.util

import me.tongfei.progressbar.ProgressBarBuilder

object ProgressBars {
    const val LENGTH = 125

    fun newBuilder(taskName: String): ProgressBarBuilder = ProgressBarBuilder()
        .setTaskName(taskName)
        .setMaxRenderedLength(LENGTH)
}