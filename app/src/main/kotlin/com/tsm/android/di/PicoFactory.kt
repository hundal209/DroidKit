package com.tsm.android.di

import com.tsm.android.catalog.JdkCatalog
import com.tsm.android.catalog.JdkCatalog.Companion.Jdk
import com.tsm.android.configuration.Configuration
import com.tsm.android.configuration.jdk.ConfigureEnvironmentVariables
import com.tsm.android.configuration.jdk.InstallJdk
import com.tsm.android.configuration.jdk.JdkConfiguration
import com.tsm.android.converter.JdkConverter
import com.tsm.android.location.GlobalScope
import com.tsm.android.location.JavaVirtualMachines
import com.tsm.android.location.JavaVirtualMachines.Companion.javaVirtualMachines
import com.tsm.android.location.UserHome
import com.tsm.android.util.*
import com.tsm.android.util.OkHttpDownloads
import org.koin.core.context.startKoin
import org.koin.dsl.bind
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
        single { JdkConverter(get()) }
    }

    private val locations = module {
        single<GlobalScope> { globalScope }
        single<UserHome> { globalScope.userHome }
        single<JavaVirtualMachines> { globalScope.userHome.javaVirtualMachines() }
    }

    private val utils = module {
        single<Logger> { defaultLogger }
        single<Distribution> { distribution }
        single<CatalogLocator> { catalogLocator }
        single<Execute> { execute }
        single<Download> {
            OkHttpDownloads(
                downloadDirectory = globalScope.userHome.resolve("Downloads"),
                logger = get(),
            )
        }
        single<Archive> { ArchiveImpl(get()) }
        single<Shell> { ExecuteShell.using(get(), globalScope.fileSystem, get()) }
    }

    private val configurations = module {
        single<Configuration<Jdk, JavaVirtualMachines>> { InstallJdk(get(), get(), get()) }
        single<Configuration<Jdk, GlobalScope>> {
            JdkConfiguration(
                installJdk = get(),
                configureEnvironmentVariables = get()
            )
        }
        single<Configuration<Jdk, UserHome>> {
            ConfigureEnvironmentVariables.from(
                shell = get(),
                javaVirtualMachines = get(),
                logger = get()
            )
        }.bind<ConfigureEnvironmentVariables>()
    }

    private val koin by lazy {
        startKoin {
            modules(catalogs, locations, utils, configurations)
        }
    }

    override fun <K : Any> create(cls: Class<K>): K {
        return koin.koin.getOrNull<K>(cls.kotlin) ?: CommandLine.defaultFactory().create(cls)
    }
}