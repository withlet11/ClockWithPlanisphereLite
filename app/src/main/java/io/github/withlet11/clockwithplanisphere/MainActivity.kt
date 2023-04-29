/*
 * MainActivity.kt
 *
 * Copyright 2020-2023 Yasuhiro Yamakawa <withlet11@gmail.com>
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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.github.withlet11.clockwithplanispherelite.widget.CwpWidget.Companion.FULL_UPDATE_INTERVAL
import io.github.withlet11.clockwithplanispherelite.widget.CwpWidget.Companion.PARTIAL_UPDATE_INTERVAL


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

    private var isClockHandsVisible = true
    private var isSouthernSky = false

    private lateinit var latitudeField: TextView
    private lateinit var longitudeField: TextView
    private lateinit var applyLocationButton: Button
    private lateinit var getLocationButton: Button
    private lateinit var statusField: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadPreviousPosition()
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        toolbar.setLogo(R.drawable.ic_launcher_foreground)
        toolbar.setTitle(R.string.app_name)

        toolbar.inflateMenu(R.menu.menu_main)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_licenses -> {
                    startActivity(Intent(application, LicenseActivity::class.java))
                }
                R.id.item_credits -> {
                    startActivity(Intent(this, OssLicensesMenuActivity::class.java))
                }
                android.R.id.home -> finish()
            }

            true
        }

        prepareGUIComponents()

        val switch1: SwitchCompat = findViewById(R.id.mode_switch)
        switch1.isChecked = isClockHandsVisible
        switch1.setOnCheckedChangeListener { _, b ->
            isClockHandsVisible = b
            with(getSharedPreferences(OBSERVATION_POSITION, Context.MODE_PRIVATE).edit()) {
                putBoolean(IS_CLOCK_HANDS_VISIBLE, isClockHandsVisible)
                apply()
            }
            val delay =
                ((System.currentTimeMillis() + 1).let { PARTIAL_UPDATE_INTERVAL - it % PARTIAL_UPDATE_INTERVAL } / 1000).toInt()
            Toast.makeText(
                applicationContext,
                resources.getQuantityString(R.plurals.clockhands_visibility_notice, delay, delay),
                Toast.LENGTH_LONG
            ).run { show() }
        }

        val switch2: SwitchCompat = findViewById(R.id.view_switch)
        switch2.isChecked = isSouthernSky
        switch2.setOnCheckedChangeListener { _, b ->
            isSouthernSky = b
            with(getSharedPreferences(OBSERVATION_POSITION, Context.MODE_PRIVATE).edit()) {
                putBoolean(IS_SOUTHERN_SKY, isSouthernSky)
                apply()
            }
            val delay =
                ((System.currentTimeMillis() + 1).let { FULL_UPDATE_INTERVAL - it % FULL_UPDATE_INTERVAL } / 1000).toInt()
            Toast.makeText(
                applicationContext,
                resources.getQuantityString(R.plurals.update_notice, delay, delay),
                Toast.LENGTH_LONG
            ).run { show() }
        }

        setLocationService()
    }

    private fun prepareGUIComponents() {
        latitudeField = findViewById<TextView>(R.id.latitudeField).apply {
            keyListener = DigitsKeyListener.getInstance("0123456789.,+-")
            setAutofillHints("%+.4f".format(23.4567))
            hint = "%+.4f".format(23.4567)
            text = "%+f".format(latitude)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    latitude = latitudeField.text.toString().replace(',', '.').toDoubleOrNull()
                    latitude?.let { if (it > 90.0 || it < -90.0) latitude = null }
                    latitudeField.setTextColor(if (latitude == null) Color.RED else Color.DKGRAY)
                    applyLocationButton.isEnabled = latitude != null && longitude != null
                }
            })
        }

        longitudeField = findViewById<TextView>(R.id.longitudeField).apply {
            keyListener = DigitsKeyListener.getInstance("0123456789.,+-")
            setAutofillHints("%+.3f".format(123.456))
            hint = "%+.3f".format(123.456)
            text = "%+f".format(longitude)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    longitude = longitudeField.text.toString().replace(',', '.').toDoubleOrNull()
                    longitude?.let { if (it > 180.0 || it < -180.0) longitude = null }
                    longitudeField.setTextColor(if (longitude == null) Color.RED else Color.DKGRAY)
                    applyLocationButton.isEnabled = latitude != null && longitude != null
                }
            })
        }

        applyLocationButton = findViewById<Button>(R.id.applyLocationButton).apply {
            setOnClickListener {
                if (latitude != null && longitude != null) {
                    getSharedPreferences(OBSERVATION_POSITION, Context.MODE_PRIVATE).edit().run {
                        putFloat(LATITUDE, latitude!!.toFloat())
                        putFloat(LONGITUDE, longitude!!.toFloat())
                        apply()
                    }
                }

                val delay =
                    ((System.currentTimeMillis() + 1).let { FULL_UPDATE_INTERVAL - it % FULL_UPDATE_INTERVAL } / 1000).toInt()
                Toast.makeText(
                    applicationContext,
                    resources.getQuantityString(R.plurals.update_notice, delay, delay),
                    Toast.LENGTH_LONG
                ).run { show() }
            }
        }

        getLocationButton = findViewById<Button>(R.id.getLocationButton).apply {
            setOnClickListener { startGPS() }
        }

        statusField = findViewById(R.id.statusField)
    }

    private fun setLocationService() {

        locationRequest = LocationRequest.create().apply {
            interval = MAXIMUM_UPDATE_INTERVAL
            fastestInterval = MINIMUM_UPDATE_INTERVAL
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation

                latitude = location?.latitude
                longitude = location?.longitude
                latitudeField.text = "%+f".format(latitude)
                longitudeField.text = "%+f".format(longitude)
                unlockViewItems()
                statusField.text = ""

                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun loadPreviousPosition() {
        val previous = getSharedPreferences(OBSERVATION_POSITION, Context.MODE_PRIVATE)

        try {
            latitude = previous.getFloat(LATITUDE, 0f).toDouble()
            longitude = previous.getFloat(LONGITUDE, 0f).toDouble()
            isSouthernSky = previous.getBoolean(IS_SOUTHERN_SKY, false)
            isClockHandsVisible = previous.getBoolean(IS_CLOCK_HANDS_VISIBLE, true)
        } catch (e: ClassCastException) {
            latitude = 0.0
            longitude = 0.0
            isSouthernSky = false
            isClockHandsVisible = true
        } finally {
        }
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
                    Looper.getMainLooper()
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
            statusField.text = getString(R.string.no_permission_to_access_location_permanent)
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
                statusField.text = getString(R.string.no_permission_to_access_location_once)
            }
        }
    }
}