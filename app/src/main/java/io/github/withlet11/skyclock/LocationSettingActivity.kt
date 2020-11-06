/*
 * LocationSettingActivity.kt
 *
 * Copyright 2020 Yasuhiro Yamakawa <withlet11@gmail.com>
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

package io.github.withlet11.skyclock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationSettingActivity : AppCompatActivity() {
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private lateinit var latitudeField: TextView
    private lateinit var longitudeField: TextView
    private lateinit var applyLocationButton: Button
    private lateinit var getLocationButton: Button
    private lateinit var statusField: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    companion object {
        const val REQUEST_PERMISSION = 1000
        private const val MAXIMUM_UPDATE_INTERVAL = 10000L
        private const val MINIMUM_UPDATE_INTERVAL = 5000L
        private const val LATITUDE = "LATITUDE"
        private const val LONGITUDE = "LONGITUDE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_setting)
        getParametersFromCaller()
        prepareGUIComponents()

        locationRequest = LocationRequest().apply {
            interval = MAXIMUM_UPDATE_INTERVAL
            fastestInterval = MINIMUM_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val location = locationResult?.lastLocation ?: return

                latitude = location.latitude
                longitude = location.longitude
                latitudeField.text = "%+f".format(latitude)
                longitudeField.text = "%+f".format(longitude)
                unlockViewItems()
                statusField.text = ""

                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        setSupportActionBar(findViewById(R.id.my_toolbar2)) // ToolBar instead of ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_cancel)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private fun getParametersFromCaller() {
        with(intent) {
            latitude = getDoubleExtra(LATITUDE, 0.0)
            longitude = getDoubleExtra(LONGITUDE, 0.0)
        }
    }

    private fun prepareGUIComponents() {
        latitudeField = findViewById<TextView>(R.id.latitudeField).apply {
            text = "%+f".format(latitude)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    latitude = latitudeField.text.toString().toDoubleOrNull()
                    latitude?.let { if (it > 90.0 || it < -90.0) latitude = null }
                    latitudeField.setTextColor(if (latitude == null) Color.RED else Color.DKGRAY)
                    applyLocationButton.isEnabled = latitude != null && longitude != null
                }
            })
        }

        longitudeField = findViewById<TextView>(R.id.longitudeField).apply {
            text = "%+f".format(longitude)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    longitude = longitudeField.text.toString().toDoubleOrNull()
                    longitude?.let { if (it > 180.0 || it < -180.0) longitude = null }
                    longitudeField.setTextColor(if (longitude == null) Color.RED else Color.DKGRAY)
                    applyLocationButton.isEnabled = latitude != null && longitude != null
                }
            })
        }

        applyLocationButton = findViewById<Button>(R.id.applyLocationButton).apply {
            setOnClickListener {
                if (latitude != null && longitude != null) {
                    with(Intent()) {
                        putExtra("LATITUDE", latitude!!)
                        putExtra("LONGITUDE", longitude!!)
                        setResult(2, this)
                    }
                } else {
                    setResult(1)
                }

                finish()
            }
        }

        getLocationButton = findViewById<Button>(R.id.getLocationButton).apply {
            setOnClickListener { startGPS() }
        }

        statusField = findViewById(R.id.statusField)
    }

    private fun lockViewItems() {
        latitudeField.isEnabled = false
        longitudeField.isEnabled = false
        applyLocationButton.isEnabled = false
        getLocationButton.isEnabled = false
    }

    private fun unlockViewItems() {
        latitudeField.isEnabled = true
        longitudeField.isEnabled = true
        applyLocationButton.isEnabled = latitude != null && longitude != null
        getLocationButton.isEnabled = true
    }

    private fun startGPS() {
        lockViewItems()
        statusField.text = getString(R.string.inGettingLocation)
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
                    null
                )
            } else {
                unlockViewItems()
                statusField.text = getString(R.string.pleaseCheckIfGPSIsOn)
            }
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION
            )
        } else {
            statusField.text = getString(R.string.no_permission_to_access_location_permanent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGPS()
            } else {
                statusField.text = getString(R.string.no_permission_to_access_location_once)
            }
        }
    }
}