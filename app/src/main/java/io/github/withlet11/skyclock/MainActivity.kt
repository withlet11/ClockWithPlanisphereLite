/*
 * MainActivity.kt
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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.github.withlet11.skyclock.fragment.NorthernSkyClockFragment
import io.github.withlet11.skyclock.fragment.SouthernSkyClockFragment


class MainActivity : AppCompatActivity() {
    var latitude = 0.0
    private var longitude = 0.0

    interface LocationChangeObserver {
        fun onLocationChange(latitude: Double, longitude: Double)
    }

    private val observers = mutableListOf<LocationChangeObserver>()

    fun addObserver(observer: LocationChangeObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: LocationChangeObserver) {
        observers.remove(observer)
    }

    private fun notifyObservers() {
        observers.forEach { it.onLocationChange(latitude, longitude) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        toolbar.setLogo(R.drawable.ic_launcher_foreground)
        toolbar.setTitle(R.string.app_name)

        toolbar.inflateMenu(R.menu.menu_main)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_settings -> {
                    val intent = Intent(application, LocationSettingActivity::class.java)
                    intent.putExtra("LATITUDE", latitude)
                    intent.putExtra("LONGITUDE", longitude)
                    startActivityForResult(intent, 2)
                }
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

        val switch: SwitchCompat = findViewById(R.id.view_switch)
        switch.setOnCheckedChangeListener { _, b ->
            val newFragment =
                (if (b) SouthernSkyClockFragment() else NorthernSkyClockFragment()).apply {
                    arguments = Bundle().apply {
                        putDouble("LATITUDE", latitude)
                        putDouble("LONGITUDE", longitude)
                    }
                }

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, newFragment)
            transaction.commit()
        }

        loadPreviousPosition()

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = NorthernSkyClockFragment().apply {
            arguments = Bundle().apply {
                putDouble("LATITUDE", latitude)
                putDouble("LONGITUDE", longitude)
            }
        }

        fragmentTransaction.add(R.id.container, fragment)
        fragmentTransaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // check if the request code is same as what is passed here it is 2
        if (requestCode == 2 && data != null) {

            try {
                latitude = data.getDoubleExtra("LATITUDE", 0.0)
                longitude = data.getDoubleExtra("LONGITUDE", 0.0)
            } catch (e: ClassCastException) {
            } finally {

                with(getSharedPreferences("observation_position", Context.MODE_PRIVATE).edit()) {
                    putFloat("latitude", latitude.toFloat())
                    putFloat("longitude", longitude.toFloat())
                    commit()
                }

                notifyObservers()
            }
        }
    }

    private fun loadPreviousPosition() {
        val previous = getSharedPreferences("observation_position", Context.MODE_PRIVATE)

        try {
            latitude = previous.getFloat("latitude", 0F).toDouble()
            longitude = previous.getFloat("longitude", 0F).toDouble()
        } catch (e: ClassCastException) {
            latitude = 0.0
            longitude = 0.0
        } finally {
        }
    }
}