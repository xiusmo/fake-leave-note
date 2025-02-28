package com.xiusm.fakeln

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import java.util.*

class LocationUtils(private val context: Context) {
    private var locationManager: LocationManager? = null
    
    fun requestLocation(
        permissionLauncher: ActivityResultLauncher<Array<String>>,
        onLocationResult: (String) -> Unit
    ) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun handlePermissionResult(
        permissions: Map<String, Boolean>,
        onLocationResult: (String) -> Unit
    ) {
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                checkLocationServiceOpen { manager ->
                    locationManager = manager
                    startLocation(manager) { location ->
                        updateDetailLocationInfo(location, onLocationResult)
                    }
                }
            }
            else -> {
                Toast.makeText(context, "需要位置权限才能使用此功能", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationServiceOpen(onSuccess: (LocationManager) -> Unit) {
        val manager = locationManager ?: context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            onSuccess(manager)
        } else {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }
    }

    private fun startLocation(
        locationManager: LocationManager,
        onLocationReceived: (Location) -> Unit
    ) {
        try {
            val criteria = Criteria()
            criteria.isAltitudeRequired = true
            val bestProvider = locationManager.getBestProvider(criteria, false)
            
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    onLocationReceived(location)
                }
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }

            bestProvider?.let { locationManager.requestLocationUpdates(it, 0L, 0f, locationListener) }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "获取位置信息失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationInfo(
        location: Location,
        onTextUpdated: (String) -> Unit
    ) {
        val locationInfo = StringBuilder().apply {
            append("time : ${location.time}\n")
            append("latitude : ${location.latitude}\n")
            append("longitude : ${location.longitude}")
        }.toString()
        
        onTextUpdated(locationInfo)
        
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val fullInfo = "$locationInfo\n地址：${address.getAddressLine(0)}"
                        onTextUpdated(fullInfo)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val fullInfo = "$locationInfo\n地址：${address.getAddressLine(0)}"
                    onTextUpdated(fullInfo)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateDetailLocationInfo(
        location: Location,
        onTextUpdated: (String) -> Unit
    ) {
        try {
            val geocoder = Geocoder(context, Locale.CHINA) // 使用中国区域设置
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val addressText = buildDetailedAddress(address)
                        onTextUpdated(addressText)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressText = buildDetailedAddress(address)
                    onTextUpdated(addressText)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onTextUpdated("获取地址失败")
        }
    }

    private fun buildDetailedAddress(address: android.location.Address): String {
        return buildString {
            // 国家
            if (!address.countryName.isNullOrEmpty()) {
                append(address.countryName)
            }
            // 省/自治区/直辖市
            if (!address.adminArea.isNullOrEmpty()) {
                append(address.adminArea)
            }
            // 市/地区
            if (!address.locality.isNullOrEmpty()) {
                append(address.locality)
            } else if (!address.subAdminArea.isNullOrEmpty()) {
                // 有些地区可能没有 locality，但有 subAdminArea
                append(address.subAdminArea)
            }
            // 区/县
            if (!address.subLocality.isNullOrEmpty()) {
                append(address.subLocality)
            }
            // 街道/镇
            if (!address.subThoroughfare.isNullOrEmpty()) {
                append(address.subThoroughfare)
            }
            // 道路/街
            if (!address.thoroughfare.isNullOrEmpty()) {
                append(address.thoroughfare)
            }
            // 门牌号
            if (!address.featureName.isNullOrEmpty() && 
                address.featureName != address.thoroughfare &&
                address.featureName != address.subThoroughfare) {
                append(address.featureName)
            }
            // 附近地标/建筑物（如果有）
            if (!address.premises.isNullOrEmpty()) {
                append(" (近")
                append(address.premises)
                append(")")
            }
        }
    }

    companion object {
        @Composable
        fun rememberLocationPermissionLauncher(
            onPermissionResult: (Map<String, Boolean>) -> Unit
        ): ActivityResultLauncher<Array<String>> {
            return rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = onPermissionResult
            )
        }
    }
}