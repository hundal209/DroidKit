package com.tsm.android.converter

import com.tsm.android.catalog.JdkCatalog
import com.tsm.android.catalog.JdkCatalog.Companion.Jdk
import picocli.CommandLine.ITypeConverter

class JdkConverter(
    private val catalog: JdkCatalog
) : ITypeConverter<Jdk> {
    override fun convert(value: String): Jdk {
        return catalog.find(value) ?: error("Invalid JDK $value")
    }
}