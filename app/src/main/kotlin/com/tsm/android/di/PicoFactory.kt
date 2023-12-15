package com.tsm.android.di

import com.tsm.android.catalog.JdkCatalog
import com.tsm.android.location.GlobalScope
import com.tsm.android.location.UserHome
import com.tsm.android.util.CatalogLocator
import com.tsm.android.util.Distribution
import com.tsm.android.util.Execute
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.slf4j.Logger
import picocli.CommandLine
import picocli.CommandLine.IFactory

/**
 * Provides access to the dependency graph of droid-kit
 */
class PicoFactory(
    private val globalScope: GlobalScope,
    private val execute: Execute,
    private val defaultLogger: Logger,
    private val distribution: Distribution,
    private val catalogLocator: CatalogLocator
) : IFactory {

    private val catalogs = module {
        single<JdkCatalog> { catalogLocator.findJdk() }
    }

    private val locations = module {
        single<GlobalScope> { globalScope }
        single<UserHome> { globalScope.userHome }
    }

    private val utils = module {
        single<Logger> { defaultLogger }
        single<Distribution> { distribution }
        single<CatalogLocator> { catalogLocator }
        single<Execute> { execute }
    }

    private val koin by lazy {
        startKoin {
            modules(catalogs, locations, utils)
        }
    }

    override fun <K : Any> create(cls: Class<K>): K {
        return koin.koin.getOrNull<K>(cls.kotlin) ?: CommandLine.defaultFactory().create(cls)
    }
}