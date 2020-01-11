package ru.shadowsparky.freezer

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppInfo(
    var isEnabled: Boolean,
    val packageName: String,
    val name: String,
    val drawable: Drawable?
) {
    companion object {
        fun getApps(packageManager: PackageManager): List<AppInfo> {
            val packages = packageManager.getInstalledPackages(0)
            val appInfos = mutableListOf<AppInfo>()
            packages.forEach {
                val isEnabled = it.applicationInfo.enabled
                val packageName = it.packageName
                val name = it.applicationInfo.loadLabel(packageManager).toString()
                val drawable =
                    it.applicationInfo.loadUnbadgedIcon(packageManager)//it.applicationInfo.loadLogo(packageManager)
                val appInfo = AppInfo(isEnabled, packageName, name, drawable)
                if (packageName != "ru.shadowsparky.freezer")
                    appInfos.add(appInfo)
            }
            return appInfos
        }
    }
}