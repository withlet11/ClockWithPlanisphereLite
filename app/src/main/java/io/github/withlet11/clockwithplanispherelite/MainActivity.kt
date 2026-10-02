/*
 * MainActivity.kt
 *
 * Copyright 2020-2026 Yasuhiro Yamakawa <withlet11@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.withlet11.clockwithplanispherelite

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import io.github.withlet11.clockwithplanispherelite.widget.CwpWidget.Companion.FULL_UPDATE_INTERVAL
import io.github.withlet11.clockwithplanispherelite.widget.CwpWidget.Companion.PARTIAL_UPDATE_INTERVAL
import androidx.core.content.edit


class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_PERMISSION = 1000
        private const val MAXIMUM_UPDATE_INTERVAL = 10000L
        private const val MINIMUM_UPDATE_INTERVAL = 5000L
        const val OBSERVATION_POSITION = "observation_position"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val IS_SOUTHERN_SKY = "isSouthernSky"
        const val IS_CLOCK_HANDS_VISIBLE = "isClockHandsVisible"
    }

    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    private var isClockHandsVisible by mutableStateOf(true)
    private var isSouthernSky by mutableStateOf(false)

    private var latitudeText by mutableStateOf("")
    private var longitudeText by mutableStateOf("")

    private var isLocationFieldsEnabled by mutableStateOf(true)
    private var statusText by mutableStateOf("")

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadPreviousPosition()
        latitudeText = "%+f".format(latitude)
        longitudeText = "%+f".format(longitude)

        setContent {
            MaterialTheme {
                MainScreen(
                    isClockHandsVisible = isClockHandsVisible,
                    onClockHandsVisibleChanged = { b ->
                        isClockHandsVisible = b
                        getSharedPreferences(OBSERVATION_POSITION, MODE_PRIVATE).edit {
                            putBoolean(IS_CLOCK_HANDS_VISIBLE, isClockHandsVisible)
                        }
                        val delay =
                            ((System.currentTimeMillis() + 1).let { PARTIAL_UPDATE_INTERVAL - it % PARTIAL_UPDATE_INTERVAL } / 1000).toInt()
                        Toast.makeText(
                            applicationContext,
                            resources.getQuantityString(
                                R.plurals.clockhands_visibility_notice,
                                delay,
                                delay
                            ),
                            Toast.LENGTH_LONG
                        ).run { show() }
                    },
                    isSouthernSky = isSouthernSky,
                    onSouthernSkyChanged = { b ->
                        isSouthernSky = b
                        getSharedPreferences(OBSERVATION_POSITION, MODE_PRIVATE).edit {
                            putBoolean(IS_SOUTHERN_SKY, isSouthernSky)
                        }
                        val delay =
                            ((System.currentTimeMillis() + 1).let { FULL_UPDATE_INTERVAL - it % FULL_UPDATE_INTERVAL } / 1000).toInt()
                        Toast.makeText(
                            applicationContext,
                            resources.getQuantityString(R.plurals.update_notice, delay, delay),
                            Toast.LENGTH_LONG
                        ).run { show() }
                    },
                    latitudeText = latitudeText,
                    onLatitudeChanged = { text ->
                        latitudeText = text
                        latitude = text.replace(',', '.').toDoubleOrNull()
                        latitude?.let { if (it > 90.0 || it < -90.0) latitude = null }
                        isLocationFieldsEnabled = true
                    },
                    longitudeText = longitudeText,
                    onLongitudeChanged = { text ->
                        longitudeText = text
                        longitude = text.replace(',', '.').toDoubleOrNull()
                        longitude?.let { if (it > 180.0 || it < -180.0) longitude = null }
                        isLocationFieldsEnabled = true
                    },
                    isLocationFieldsEnabled = isLocationFieldsEnabled,
                    onApplyLocation = {
                        if (latitude != null && longitude != null) {
                            getSharedPreferences(OBSERVATION_POSITION, MODE_PRIVATE).edit {
                                putFloat(LATITUDE, latitude!!.toFloat())
                                putFloat(LONGITUDE, longitude!!.toFloat())
                            }
                        }

                        val delay =
                            ((System.currentTimeMillis() + 1).let { FULL_UPDATE_INTERVAL - it % FULL_UPDATE_INTERVAL } / 1000).toInt()
                        Toast.makeText(
                            applicationContext,
                            resources.getQuantityString(R.plurals.update_notice, delay, delay),
                            Toast.LENGTH_LONG
                        ).run { show() }
                    },
                    onGetLocation = { startGPS() },
                    statusText = statusText,
                    onLicensesClick = {
                        startActivity(Intent(application, LicenseActivity::class.java))
                    },
                    onPrivacyPolicyClick = {
                        startActivity(Intent(application, PrivacyPolicyActivity::class.java))
                    },
                    onCreditsClick = {
                        startActivity(Intent(this, OssLicensesMenuActivity::class.java))
                    },
                    onFinish = { finish() }
                )
            }
        }

        setLocationService()
    }

    private fun setLocationService() {
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, MAXIMUM_UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(MINIMUM_UPDATE_INTERVAL)
                .setWaitForAccurateLocation(true)
                .build()


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation

                latitude = location?.latitude
                longitude = location?.longitude
                latitudeText = "%+f".format(latitude)
                longitudeText = "%+f".format(longitude)
                unlockViewItems()
                statusText = ""

                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun loadPreviousPosition() {
        val previous = getSharedPreferences(OBSERVATION_POSITION, MODE_PRIVATE)

        try {
            latitude = previous.getFloat(LATITUDE, 0f).toDouble()
            longitude = previous.getFloat(LONGITUDE, 0f).toDouble()
            isSouthernSky = previous.getBoolean(IS_SOUTHERN_SKY, false)
            isClockHandsVisible = previous.getBoolean(IS_CLOCK_HANDS_VISIBLE, true)
        } catch (_: ClassCastException) {
            latitude = 0.0
            longitude = 0.0
            isSouthernSky = false
            isClockHandsVisible = true
        } finally {
        }
    }

    private fun lockViewItems() {
        isLocationFieldsEnabled = false
    }

    private fun unlockViewItems() {
        isLocationFieldsEnabled = true
    }

    private fun startGPS() {
        lockViewItems()
        statusText = getString(R.string.inGettingLocation)
        val isPermissionFineLocation = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val isPermissionCoarseLocation = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (isPermissionFineLocation != PackageManager.PERMISSION_GRANTED &&
            isPermissionCoarseLocation != PackageManager.PERMISSION_GRANTED
        ) {
            unlockViewItems()
            requestLocationPermission()
        } else {
            val locationManager: LocationManager =
                applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                unlockViewItems()
                statusText = getString(R.string.pleaseCheckIfGPSIsOn)
            }
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            statusText = getString(R.string.no_permission_to_access_location_permanent)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGPS()
            } else {
                statusText = getString(R.string.no_permission_to_access_location_once)
            }
        }
    }
}
