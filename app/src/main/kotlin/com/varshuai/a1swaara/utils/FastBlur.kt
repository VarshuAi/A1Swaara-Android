package com.varshuai.a1swaara.utils

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.varshuai.a1swaara.ui.components.SwaaraDeepSurface
import com.varshuai.a1swaara.ui.components.SwaaraMagenta
import com.varshuai.a1swaara.ui.components.SwaaraViolet

data class AmbientColors(
    val dominant: Color,
    val vibrant: Color,
    val darkVibrant: Color
)

object FastBlur {

    fun extractAmbientColors(bitmap: Bitmap): AmbientColors {
        return try {
            val palette = Palette.from(bitmap)
                .maximumColorCount(16)
                .generate()

            val dominantInt = palette.dominantSwatch?.rgb
            val vibrantInt = palette.vibrantSwatch?.rgb ?: palette.lightVibrantSwatch?.rgb
            val darkVibrantInt = palette.darkVibrantSwatch?.rgb ?: palette.darkMutedSwatch?.rgb

            AmbientColors(
                dominant = if (dominantInt != null) Color(dominantInt) else SwaaraViolet,
                vibrant = if (vibrantInt != null) Color(vibrantInt) else SwaaraMagenta,
                darkVibrant = if (darkVibrantInt != null) Color(darkVibrantInt) else SwaaraDeepSurface
            )
        } catch (_: Throwable) {
            AmbientColors(
                dominant = SwaaraViolet,
                vibrant = SwaaraMagenta,
                darkVibrant = SwaaraDeepSurface
            )
        }
    }

    /**
     * StackBlur algorithm (Mario Klingemann).
     * Fast in-memory CPU blur with O(N) complexity.
     * Perfect for downscaled bitmaps (e.g. 48x48) taking < 0.3ms to execute.
     */
    fun stackBlur(sentBitmap: Bitmap, radius: Int = 10, canReuse: Boolean = false): Bitmap {
        val r = radius.coerceAtLeast(1)
        val bitmap = if (canReuse) sentBitmap else sentBitmap.copy(sentBitmap.config ?: Bitmap.Config.ARGB_8888, true)
        val w = bitmap.width
        val h = bitmap.height

        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = r + r + 1

        val rArr = IntArray(wh)
        val gArr = IntArray(wh)
        val bArr = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(maxOf(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        for (idx in 0 until 256 * divsum) {
            dv[idx] = (idx / divsum)
        }

        yi = 0
        yw = 0

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        for (yIdx in 0 until h) {
            rinsum = 0
            ginsum = 0
            binsum = 0
            var routsumLocal = 0
            var goutsumLocal = 0
            var boutsumLocal = 0
            rsum = 0
            gsum = 0
            bsum = 0
            for (offset in -r..r) {
                p = pix[yi + minOf(wm, maxOf(offset, 0))]
                val sir = stack[offset + r]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)
                val rbsVal = r + 1 - kotlin.math.abs(offset)
                rsum += sir[0] * rbsVal
                gsum += sir[1] * rbsVal
                bsum += sir[2] * rbsVal
                if (offset > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsumLocal += sir[0]
                    goutsumLocal += sir[1]
                    boutsumLocal += sir[2]
                }
            }
            stackpointer = r

            for (xIdx in 0 until w) {
                rArr[yi] = dv[rsum]
                gArr[yi] = dv[gsum]
                bArr[yi] = dv[bsum]

                rsum -= routsumLocal
                gsum -= goutsumLocal
                bsum -= boutsumLocal

                stackstart = stackpointer - r + div
                val sir = stack[stackstart % div]

                routsumLocal -= sir[0]
                goutsumLocal -= sir[1]
                boutsumLocal -= sir[2]

                if (yIdx == 0) {
                    vmin[xIdx] = minOf(xIdx + r + 1, wm)
                }
                p = pix[yw + vmin[xIdx]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                val sir2 = stack[stackpointer % div]

                routsumLocal += sir2[0]
                goutsumLocal += sir2[1]
                boutsumLocal += sir2[2]

                rinsum -= sir2[0]
                ginsum -= sir2[1]
                binsum -= sir2[2]

                yi++
            }
            yw += w
        }

        for (xIdx in 0 until w) {
            rinsum = 0
            ginsum = 0
            binsum = 0
            var routsumLocal = 0
            var goutsumLocal = 0
            var boutsumLocal = 0
            rsum = 0
            gsum = 0
            bsum = 0
            yp = -r * w
            for (offset in -r..r) {
                yi = maxOf(0, yp) + xIdx
                val sir = stack[offset + r]
                sir[0] = rArr[yi]
                sir[1] = gArr[yi]
                sir[2] = bArr[yi]
                val rbsVal = r + 1 - kotlin.math.abs(offset)
                rsum += rArr[yi] * rbsVal
                gsum += gArr[yi] * rbsVal
                bsum += bArr[yi] * rbsVal
                if (offset > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsumLocal += sir[0]
                    goutsumLocal += sir[1]
                    boutsumLocal += sir[2]
                }
                if (offset < hm) {
                    yp += w
                }
            }
            yi = xIdx
            stackpointer = r
            for (yIdx in 0 until h) {
                pix[yi] = (-0x1000000 and pix[yi]) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsumLocal
                gsum -= goutsumLocal
                bsum -= boutsumLocal

                stackstart = stackpointer - r + div
                val sir = stack[stackstart % div]

                routsumLocal -= sir[0]
                goutsumLocal -= sir[1]
                boutsumLocal -= sir[2]

                if (xIdx == 0) {
                    vmin[yIdx] = minOf(yIdx + r + 1, hm) * w
                }
                p = xIdx + vmin[yIdx]

                sir[0] = rArr[p]
                sir[1] = gArr[p]
                sir[2] = bArr[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                val sir2 = stack[stackpointer]

                routsumLocal += sir2[0]
                goutsumLocal += sir2[1]
                boutsumLocal += sir2[2]

                rinsum -= sir2[0]
                ginsum -= sir2[1]
                binsum -= sir2[2]

                yi += w
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }
}
