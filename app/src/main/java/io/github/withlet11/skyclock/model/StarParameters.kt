/*
 * StarParameters.kt
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

package io.github.withlet11.skyclock.model

class StarParameters(hipNumber: String,
                     ra_h: String,
                     ra_m: String,
                     ra_s: String,
                     dec_sign: String,
                     dec_deg: String,
                     dec_m: String,
                     dec_s: String,
                     magnitude: String
) {
    var hipNumber = 0
    var ra = 0.0
    var dec = 0.0
    var magnitude = 0.0

    init {
        try {
            this.hipNumber = hipNumber.toInt()
            this.magnitude = magnitude.toDouble()
            ra = ra_h.toInt() + ra_m.toInt() / 60.0 + ra_s.toDouble() / 3600.0
            dec = (if (dec_sign == "1") +1.0 else -1.0) * (dec_deg.toInt() + dec_m.toInt() / 60.0 + dec_s.toDouble() / 3600.0)
        } catch (e: TypeCastException) {
            println("error hip: $hipNumber")
        }
    }
}