package com.tsm.android.util

import com.tsm.android.catalog.JdkCatalog

/**
 * [CatalogLocator] provides access to reading the catalogs from
 * [Distribution]
 */
class CatalogLocator(private val distribution: Distribution) {
    fun findJdk() = JdkCatalog.of(distribution.findCatalog("jdk-catalog.json"))
}