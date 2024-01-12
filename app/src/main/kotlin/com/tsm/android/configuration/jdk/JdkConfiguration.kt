package com.tsm.android.configuration.jdk

import com.tsm.android.catalog.JdkCatalog.Companion.Jdk
import com.tsm.android.configuration.Configuration
import com.tsm.android.location.GlobalScope
import com.tsm.android.location.JavaVirtualMachines
import com.tsm.android.location.JavaVirtualMachines.Companion.javaVirtualMachines
import com.tsm.android.util.Status

class JdkConfiguration(
    private val installJdk: Configuration<Jdk, JavaVirtualMachines>,
    private val configureEnvironmentVariables: ConfigureEnvironmentVariables
) : Configuration<Jdk, GlobalScope> {

    override fun applyFor(chosen: Jdk, to: GlobalScope): Status {
        return installJdk.applyFor(chosen, to.javaVirtualMachines()) +
                configureEnvironmentVariables.applyFor(chosen, to.userHome)
    }
}