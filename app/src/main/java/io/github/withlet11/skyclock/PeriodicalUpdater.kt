/*
 * PeriodicalUpdater.kt
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

import android.os.Handler
import io.github.withlet11.skyclock.fragment.AbstractSkyClockFragment


class PeriodicalUpdater(private val skyClockFragment: AbstractSkyClockFragment) {
    companion object {
        const val PERIOD = 100L
    }

    private lateinit var runnable: Runnable
    private val handler = Handler()
    // private var counter = 0

    fun timerSet() {
        runnable = object : Runnable {
            override fun run() {
                skyClockFragment.updateClockIfClockHandsAreVisible()
                /*
                if (++counter > 255) {
                    skyClockFragment.updateSkyPanel()
                    counter = 0
                }

                 */
                handler.postDelayed(this, PERIOD)
            }
        }
        handler.post(runnable)
    }

    fun stopTimerTask() {
        handler.removeCallbacks(runnable)
    }
}