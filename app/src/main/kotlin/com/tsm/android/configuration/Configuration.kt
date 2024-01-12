package com.tsm.android.configuration

import com.tsm.android.location.Location
import com.tsm.android.util.Status

/**
 * [Configuration] for a chosen model
 */
interface Configuration<MODEL: Any, LOCATION: Location> {
    fun applyFor(
        chosen: MODEL,
        to: LOCATION
    ): Status
}