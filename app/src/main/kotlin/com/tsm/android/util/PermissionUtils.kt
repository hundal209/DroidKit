package com.tsm.android.util

import java.nio.file.attribute.PosixFilePermission
import java.util.EnumSet

object PermissionUtils {

    /**
     * Translates a Unix stat(2) `st_mode` compatible value into a set of permissions
     * @param mode the "mode"
     * @return set of permissions
      */
    fun permissionsFromMode(mode: Int): Set<PosixFilePermission> {
        val permissions: MutableSet<PosixFilePermission> = EnumSet.noneOf(PosixFilePermission::class.java)
        addPermissions(permissions, "OTHERS", mode)
        addPermissions(permissions, "GROUP", mode shr 3)
        addPermissions(permissions, "OWNER", mode shr 6)
        return permissions
    }

    private fun addPermissions(
        permissions: MutableSet<PosixFilePermission>,
        prefix: String,
        mode: Int
    ) {
        if (mode and 1 == 1) {
            permissions.add(PosixFilePermission.valueOf(prefix + "_EXECUTE"))
        }
        if (mode and 2 == 2) {
            permissions.add(PosixFilePermission.valueOf(prefix + "_WRITE"))
        }
        if (mode and 4 == 4) {
            permissions.add(PosixFilePermission.valueOf(prefix + "_READ"))
        }
    }
}