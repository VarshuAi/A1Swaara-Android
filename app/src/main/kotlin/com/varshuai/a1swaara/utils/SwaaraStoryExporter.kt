package com.varshuai.a1swaara.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.service.PlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.ByteBuffer

object SwaaraStoryExporter {
    private const val TAG = "SwaaraStoryExporter"
    const val WEB_SHARE_BASE = "https://a1swaara.vercel.app/s"

    /** Loads artwork bitmap asynchronously from URL or cache with software config */
    suspend fun loadArtworkBitmap(context: Context, artworkUrl: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (artworkUrl.isNullOrBlank()) return@withContext null
        try {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(artworkUrl)
                .allowHardware(false)
                .build()
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                val bmp = result.image.toBitmap()
                return@withContext ensureSoftwareBitmap(bmp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Coil failed to load artwork, trying direct connection", e)
        }

        try {
            val url = URL(artworkUrl)
            val connection = url.openConnection().apply {
                connectTimeout = 5000
                readTimeout = 5000
            }
            connection.getInputStream().use { input ->
                val bmp = BitmapFactory.decodeStream(input)
                if (bmp != null) return@withContext ensureSoftwareBitmap(bmp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Direct stream failed to load artwork", e)
        }
        null
    }

    private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }
    }

    /** Creates a rounded corner bitmap safely with software canvas */
    private fun getRoundedCornerBitmap(rawBitmap: Bitmap, cornerRadius: Float): Bitmap {
        val bitmap = ensureSoftwareBitmap(rawBitmap)
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    /** Generates the 720x1280 Story Card bitmap */
    suspend fun createStoryCardBitmap(
        context: Context,
        title: String,
        artist: String,
        artworkUrl: String?,
        frameIndex: Int = 0
    ): Bitmap = withContext(Dispatchers.IO) {
        val width = 720
        val height = 1280
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Dark Gradient Canvas Backdrop
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(0xFF08080C.toInt(), 0xFF141422.toInt(), 0xFF050508.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Ambient Neon Magenta & Violet Glow Orbs
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33FF2DAA.toInt()
        }
        canvas.drawCircle(width * 0.25f, height * 0.35f, 280f, glowPaint)
        glowPaint.color = 0x2E8B35FF.toInt()
        canvas.drawCircle(width * 0.75f, height * 0.58f, 300f, glowPaint)

        // 3. Top A1 SWAARA Branding Glass Capsule
        val badgeRect = RectF(width / 2f - 180f, 120f, width / 2f + 180f, 180f)
        val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x99191924.toInt()
        }
        canvas.drawRoundRect(badgeRect, 30f, 30f, badgeBg)

        val badgeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = 0x40FFFFFF.toInt()
        }
        canvas.drawRoundRect(badgeRect, 30f, 30f, badgeBorder)

        val brandTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("A1 SWAARA STUDIO", width / 2f, 158f, brandTextPaint)

        // 4. Central Album Artwork Card
        val cardSize = 500f
        val artLeft = (width - cardSize) / 2f
        val artTop = height * 0.22f
        val artRect = RectF(artLeft, artTop, artLeft + cardSize, artTop + cardSize)

        val artBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = 0x80FF2DAA.toInt()
        }
        canvas.drawRoundRect(artRect, 36f, 36f, artBorder)

        val rawArtworkBmp = loadArtworkBitmap(context, artworkUrl)
        if (rawArtworkBmp != null) {
            val roundedBmp = getRoundedCornerBitmap(rawArtworkBmp, 36f)
            val srcRect = Rect(0, 0, roundedBmp.width, roundedBmp.height)
            canvas.drawBitmap(roundedBmp, srcRect, artRect, Paint(Paint.FILTER_BITMAP_FLAG))
            if (roundedBmp != rawArtworkBmp) roundedBmp.recycle()
            rawArtworkBmp.recycle()
        } else {
            val cardBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF191924.toInt()
            }
            canvas.drawRoundRect(artRect, 36f, 36f, cardBg)
            val discPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = 0x60FF2DAA.toInt()
            }
            val cx = width / 2f
            val cy = artTop + cardSize / 2f
            canvas.drawCircle(cx, cy, 120f, discPaint)
            canvas.drawCircle(cx, cy, 75f, discPaint)
            discPaint.style = Paint.Style.FILL
            discPaint.color = 0xFFFF2DAA.toInt()
            canvas.drawCircle(cx, cy, 22f, discPaint)
        }

        // 5. Track Title & Artist
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val safeTitle = if (title.length > 26) title.take(24) + "..." else title
        canvas.drawText(safeTitle, width / 2f, height * 0.70f, titlePaint)

        val artistPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFA0A0B5.toInt()
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        val safeArtist = if (artist.length > 32) artist.take(30) + "..." else artist
        canvas.drawText(safeArtist, width / 2f, height * 0.74f, artistPaint)

        // 6. Dynamic Equalizer Waveform Bars
        val eqPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 10f
            color = 0xFF20CFFF.toInt()
        }
        val barCount = 11
        val startX = width / 2f - (barCount * 28f) / 2f
        val baseHeights = floatArrayOf(25f, 55f, 38f, 80f, 48f, 95f, 52f, 70f, 32f, 60f, 25f)

        for (idx in 0 until barCount) {
            val phase = (frameIndex * 0.25f + idx * 0.5f)
            val scale = (0.35f + 0.65f * kotlin.math.abs(kotlin.math.sin(phase.toDouble()))).toFloat()
            val barH = baseHeights[idx] * scale
            val x = startX + (idx * 28f)
            canvas.drawLine(x, height * 0.83f - barH / 2f, x, height * 0.83f + barH / 2f, eqPaint)
        }

        // 7. Bottom Stamp
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x99FFFFFF.toInt()
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Listen in Ultra HQ (320 kbps) on A1 Swaara", width / 2f, height - 90f, footerPaint)

        bitmap
    }

    /**
     * Converts ARGB pixel array to YUV420SemiPlanar (NV12) for universal hardware AVC encoders.
     * NV12: Y plane followed by interleaved U and V bytes.
     */
    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var aIndex = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                val rgb = argb[aIndex++]
                val r = (rgb shr 16) and 0xff
                val g = (rgb shr 8) and 0xff
                val b = rgb and 0xff

                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128

                yuv420sp[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                if (j % 2 == 0 && i % 2 == 0) {
                    // NV12 format: U first, then V
                    yuv420sp[uvIndex++] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                    yuv420sp[uvIndex++] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                }
            }
        }
    }

    private class TrackIndices(
        var videoTrackIndex: Int = -1,
        var audioTrackIndex: Int = -1,
        var audioFormat: MediaFormat? = null,
        var muxerStarted: Boolean = false
    )

    private fun drainEncoder(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        trackIndices: TrackIndices,
        endOfStream: Boolean
    ) {
        val timeoutUs = if (endOfStream) 10000L else 2000L
        while (true) {
            val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!trackIndices.muxerStarted) {
                    trackIndices.videoTrackIndex = muxer.addTrack(encoder.outputFormat)
                    val aFormat = trackIndices.audioFormat
                    if (aFormat != null && trackIndices.audioTrackIndex < 0) {
                        try {
                            trackIndices.audioTrackIndex = muxer.addTrack(aFormat)
                        } catch (e: Throwable) {
                            Log.w(TAG, "Audio track could not be added to muxer: ${e.message}")
                        }
                    }
                    muxer.start()
                    trackIndices.muxerStarted = true
                }
            } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (outputBufferIndex >= 0) {
                val outputBuffer = encoder.getOutputBuffer(outputBufferIndex)
                if (outputBuffer != null && bufferInfo.size > 0 && trackIndices.muxerStarted) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        muxer.writeSampleData(trackIndices.videoTrackIndex, outputBuffer, bufferInfo)
                    }
                }
                encoder.releaseOutputBuffer(outputBufferIndex, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
    }

    /**
     * Generates a real native MP4 Story Video with 100% crash-proof byte-buffer YUV codec,
     * zero-allocation frame loop, preloaded artwork, and optional audio track muxing.
     */
    suspend fun generateStoryVideo(
        context: Context,
        title: String,
        artist: String,
        artworkUrl: String?,
        durationSeconds: Int = 15,
        snippetStartSec: Float = 0f,
        videoId: String? = null,
        onProgress: (Int) -> Unit = {}
    ): File? = withContext(Dispatchers.IO) {
        val width = 720
        val height = 1280
        val fps = 15
        val totalFrames = durationSeconds * fps
        val bitRate = 2000000 // 2.0 Mbps
        val cachePath = File(context.cacheDir, "stories").apply { mkdirs() }
        val outputFile = File(cachePath, "swaara_story_${System.currentTimeMillis()}.mp4")

        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var extractor: MediaExtractor? = null
        var baseBitmap: Bitmap? = null
        var frameBitmap: Bitmap? = null
        val trackIndices = TrackIndices()
        val bufferInfo = MediaCodec.BufferInfo()

        try {
            onProgress(5)

            // Step 1: Check if audio exists in cache and can be muxed (AAC)
            if (!videoId.isNullOrBlank()) {
                try {
                    val cache = PlayerService.sharedCache(context)
                    val spans = cache.getCachedSpans(videoId)
                    val audioFile = spans.firstOrNull { it.file != null && it.file!!.exists() }?.file
                    if (audioFile != null) {
                        val testExtractor = MediaExtractor().apply {
                            setDataSource(audioFile.absolutePath)
                        }
                        for (i in 0 until testExtractor.trackCount) {
                            val format = testExtractor.getTrackFormat(i)
                            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                            if (mime.startsWith("audio/") && (mime.contains("mp4a") || mime.contains("aac"))) {
                                trackIndices.audioFormat = format
                                testExtractor.selectTrack(i)
                                extractor = testExtractor
                                break
                            }
                        }
                        if (extractor == null) {
                            testExtractor.release()
                        }
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "Audio discovery exception: ${e.message}")
                    try { extractor?.release() } catch (_: Throwable) {}
                    extractor = null
                }
            }

            // Step 2: Pre-render the static background card ONCE
            baseBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val baseCanvas = Canvas(baseBitmap)

            val bgPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(0xFF08080C.toInt(), 0xFF141422.toInt(), 0xFF050508.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            baseCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33FF2DAA.toInt() }
            baseCanvas.drawCircle(width * 0.25f, height * 0.35f, 280f, glowPaint)
            glowPaint.color = 0x2E8B35FF.toInt()
            baseCanvas.drawCircle(width * 0.75f, height * 0.58f, 300f, glowPaint)

            val badgeRect = RectF(width / 2f - 180f, 120f, width / 2f + 180f, 180f)
            val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x99191924.toInt() }
            baseCanvas.drawRoundRect(badgeRect, 30f, 30f, badgeBg)

            val badgeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = 0x40FFFFFF.toInt()
            }
            baseCanvas.drawRoundRect(badgeRect, 30f, 30f, badgeBorder)

            val brandTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 24f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            baseCanvas.drawText("A1 SWAARA STUDIO", width / 2f, 158f, brandTextPaint)

            val cardSize = 500f
            val artLeft = (width - cardSize) / 2f
            val artTop = height * 0.22f
            val artRect = RectF(artLeft, artTop, artLeft + cardSize, artTop + cardSize)

            val artBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
                color = 0x80FF2DAA.toInt()
            }
            baseCanvas.drawRoundRect(artRect, 36f, 36f, artBorder)

            val rawArtworkBmp = loadArtworkBitmap(context, artworkUrl)
            if (rawArtworkBmp != null) {
                val roundedBmp = getRoundedCornerBitmap(rawArtworkBmp, 36f)
                val srcRect = Rect(0, 0, roundedBmp.width, roundedBmp.height)
                baseCanvas.drawBitmap(roundedBmp, srcRect, artRect, Paint(Paint.FILTER_BITMAP_FLAG))
                if (roundedBmp != rawArtworkBmp) roundedBmp.recycle()
                rawArtworkBmp.recycle()
            } else {
                val cardBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF191924.toInt() }
                baseCanvas.drawRoundRect(artRect, 36f, 36f, cardBg)
                androidx.appcompat.content.res.AppCompatResources.getDrawable(context, R.drawable.musical_notes)?.let { drawable ->
                    val iconSize = 120
                    val left = (width - iconSize) / 2
                    val top = (artTop + 220f).toInt()
                    drawable.setBounds(left, top, left + iconSize, top + iconSize)
                    drawable.setTint(0xFFFF2DAA.toInt())
                    drawable.draw(baseCanvas)
                }
            }

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 40f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            val safeTitle = if (title.length > 26) title.take(24) + "..." else title
            baseCanvas.drawText(safeTitle, width / 2f, height * 0.70f, titlePaint)

            val artistPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFA0A0B5.toInt()
                textSize = 26f
                textAlign = Paint.Align.CENTER
            }
            val safeArtist = if (artist.length > 32) artist.take(30) + "..." else artist
            baseCanvas.drawText(safeArtist, width / 2f, height * 0.74f, artistPaint)

            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x99FFFFFF.toInt()
                textSize = 22f
                textAlign = Paint.Align.CENTER
            }
            baseCanvas.drawText("Listen in Ultra HQ (320 kbps) on A1 Swaara", width / 2f, height - 90f, footerPaint)

            onProgress(15)

            // Step 3: Initialize Video Encoder & Muxer
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            frameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val frameCanvas = Canvas(frameBitmap)

            val eqPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                strokeCap = Paint.Cap.ROUND
                strokeWidth = 10f
                color = 0xFF20CFFF.toInt()
            }
            val barCount = 11
            val startX = width / 2f - (barCount * 28f) / 2f
            val baseHeights = floatArrayOf(25f, 55f, 38f, 80f, 48f, 95f, 52f, 70f, 32f, 60f, 25f)
            val eqCenterY = height * 0.83f

            val argbPixels = IntArray(width * height)
            val yuvBytes = ByteArray(width * height * 3 / 2)

            // Step 4: Zero-allocation frame loop
            for (frame in 0 until totalFrames) {
                frameCanvas.drawBitmap(baseBitmap, 0f, 0f, null)

                for (idx in 0 until barCount) {
                    val phase = (frame * 0.25f + idx * 0.5f)
                    val scale = (0.35f + 0.65f * kotlin.math.abs(kotlin.math.sin(phase.toDouble()))).toFloat()
                    val barH = baseHeights[idx] * scale
                    val x = startX + (idx * 28f)
                    frameCanvas.drawLine(x, eqCenterY - barH / 2f, x, eqCenterY + barH / 2f, eqPaint)
                }

                frameBitmap.getPixels(argbPixels, 0, width, 0, 0, width, height)
                encodeYUV420SP(yuvBytes, argbPixels, width, height)

                val isLastFrame = (frame == totalFrames - 1)
                val pts = frame * 1_000_000L / fps

                var queued = false
                var retryCount = 0
                while (!queued && retryCount < 50) {
                    val inputBufferIndex = encoder.dequeueInputBuffer(5000)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = encoder.getInputBuffer(inputBufferIndex)
                        inputBuffer?.clear()
                        inputBuffer?.put(yuvBytes)
                        val flags = if (isLastFrame) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                        encoder.queueInputBuffer(inputBufferIndex, 0, yuvBytes.size, pts, flags)
                        queued = true
                    } else {
                        drainEncoder(encoder, muxer, bufferInfo, trackIndices, false)
                        retryCount++
                    }
                }

                drainEncoder(encoder, muxer, bufferInfo, trackIndices, false)

                if (frame % (fps / 2) == 0 || isLastFrame) {
                    val percent = 15 + ((frame.toFloat() / totalFrames) * 75).toInt()
                    onProgress(percent.coerceAtMost(90))
                }
            }

            // Flush remaining video output buffers
            drainEncoder(encoder, muxer, bufferInfo, trackIndices, true)

            // Step 5: Mux audio track if available
            if (extractor != null && trackIndices.audioTrackIndex >= 0 && trackIndices.muxerStarted) {
                onProgress(92)
                try {
                    val startUs = (snippetStartSec * 1_000_000L).toLong()
                    val endUs = ((snippetStartSec + durationSeconds) * 1_000_000L).toLong()
                    extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                    val audioBuffer = ByteBuffer.allocate(64 * 1024)
                    val audioBufferInfo = MediaCodec.BufferInfo()
                    var baseAudioPts = -1L

                    while (true) {
                        val sampleSize = extractor.readSampleData(audioBuffer, 0)
                        if (sampleSize < 0) break
                        val sampleTime = extractor.sampleTime
                        if (sampleTime > endUs) break
                        if (sampleTime >= startUs) {
                            if (baseAudioPts < 0) baseAudioPts = sampleTime
                            audioBufferInfo.offset = 0
                            audioBufferInfo.size = sampleSize
                            audioBufferInfo.presentationTimeUs = (sampleTime - baseAudioPts).coerceAtLeast(0L)
                            audioBufferInfo.flags = extractor.sampleFlags
                            muxer.writeSampleData(trackIndices.audioTrackIndex, audioBuffer, audioBufferInfo)
                        }
                        if (!extractor.advance()) break
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "Audio sample muxing exception: ${e.message}", e)
                }
            }

            onProgress(100)
            return@withContext outputFile
        } catch (e: Throwable) {
            Log.e(TAG, "MediaCodec video generation failed", e)
            return@withContext null
        } finally {
            try {
                if (trackIndices.muxerStarted && muxer != null) {
                    muxer.stop()
                }
            } catch (e: Throwable) {
                Log.w(TAG, "MediaMuxer stop failed: ${e.message}")
            }
            try { muxer?.release() } catch (_: Throwable) {}
            try { encoder?.stop() } catch (_: Throwable) {}
            try { encoder?.release() } catch (_: Throwable) {}
            try { extractor?.release() } catch (_: Throwable) {}
            try { baseBitmap?.recycle() } catch (_: Throwable) {}
            try { frameBitmap?.recycle() } catch (_: Throwable) {}
        }
    }

    fun buildSongShareUrl(videoId: String?, title: String = "", artist: String = "", artworkUrl: String? = null): String {
        if (videoId.isNullOrBlank()) return "https://a1swaara.vercel.app"
        return "$WEB_SHARE_BASE/$videoId"
    }


    /** Shares Image Story directly via Intent */
    suspend fun shareImageStory(
        context: Context,
        title: String,
        artist: String,
        artworkUrl: String?,
        videoId: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val bitmap = createStoryCardBitmap(context, title, artist, artworkUrl)
            val cachePath = File(context.cacheDir, "stories").apply { mkdirs() }
            val file = File(cachePath, "story_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val shareUrl = buildSongShareUrl(videoId, title, artist, artworkUrl)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Listening to \"$title\" by $artist on A1 Swaara\n$shareUrl")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Story Card to...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            withContext(Dispatchers.Main) {
                context.startActivity(chooser)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to share story image", e)
            withContext(Dispatchers.Main) {
                context.toast("Failed to share story: ${e.localizedMessage}")
            }
        }
    }

    /** Saves Image Story card directly to device gallery with crash-proof fallback */
    suspend fun saveImageToGallery(
        context: Context,
        title: String,
        artist: String,
        artworkUrl: String?
    ) = withContext(Dispatchers.IO) {
        try {
            val bitmap = createStoryCardBitmap(context, title, artist, artworkUrl)
            val cleanName = title.replace(Regex("[^a-zA-Z0-9]"), "_")
            val filename = "A1_Swaara_${cleanName}_${System.currentTimeMillis()}.png"
            var saved = false

            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/A1_Swaara")
                    }
                }
                val imageUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                if (imageUri != null) {
                    context.contentResolver.openOutputStream(imageUri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        saved = true
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "MediaStore insert failed, using fallback storage", e)
            }

            if (!saved) {
                val fallbackDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "A1_Swaara").apply { mkdirs() }
                val targetFile = File(fallbackDir, filename)
                FileOutputStream(targetFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    saved = true
                }
                MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), arrayOf("image/png"), null)
            }

            withContext(Dispatchers.Main) {
                context.toast("Story Card saved to Gallery")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to save image to gallery", e)
            withContext(Dispatchers.Main) {
                context.toast("Failed to save image: ${e.localizedMessage}")
            }
        }
    }

    /** Shares Video Story */
    suspend fun shareVideoStory(
        context: Context,
        title: String,
        artist: String,
        artworkUrl: String?,
        durationSeconds: Int = 15,
        snippetStartSec: Float = 0f,
        videoId: String? = null,
        onProgress: (Int) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        try {
            val videoFile = generateStoryVideo(
                context = context,
                title = title,
                artist = artist,
                artworkUrl = artworkUrl,
                durationSeconds = durationSeconds,
                snippetStartSec = snippetStartSec,
                videoId = videoId,
                onProgress = onProgress
            )
            if (videoFile != null && videoFile.exists()) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", videoFile)
                val shareUrl = buildSongShareUrl(videoId, title, artist, artworkUrl)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/mp4"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Now Playing: $title — $artist\n$shareUrl")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "Share Music Story Video to...").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                withContext(Dispatchers.Main) {
                    context.startActivity(chooser)
                }
            } else {
                shareImageStory(context, title, artist, artworkUrl, videoId)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in shareVideoStory", e)
            shareImageStory(context, title, artist, artworkUrl, videoId)
        }
    }

    /** Saves Video Story directly to Gallery Movies with crash-proof fallback */
    suspend fun saveVideoToGallery(
        context: Context,
        title: String,
        artist: String,
        artworkUrl: String?,
        durationSeconds: Int = 15,
        snippetStartSec: Float = 0f,
        videoId: String? = null,
        onProgress: (Int) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        try {
            val videoFile = generateStoryVideo(
                context = context,
                title = title,
                artist = artist,
                artworkUrl = artworkUrl,
                durationSeconds = durationSeconds,
                snippetStartSec = snippetStartSec,
                videoId = videoId,
                onProgress = onProgress
            )
            if (videoFile != null && videoFile.exists()) {
                val cleanName = title.replace(Regex("[^a-zA-Z0-9]"), "_")
                val filename = "A1_Swaara_${cleanName}_${System.currentTimeMillis()}.mp4"
                var saved = false

                try {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/A1_Swaara")
                        }
                    }
                    val videoUri = context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                    if (videoUri != null) {
                        context.contentResolver.openOutputStream(videoUri)?.use { out ->
                            videoFile.inputStream().use { input ->
                                input.copyTo(out)
                                saved = true
                            }
                        }
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "MediaStore video insert failed, using fallback storage", e)
                }

                if (!saved) {
                    val fallbackDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "A1_Swaara").apply { mkdirs() }
                    val targetFile = File(fallbackDir, filename)
                    FileOutputStream(targetFile).use { out ->
                        videoFile.inputStream().use { input ->
                            input.copyTo(out)
                            saved = true
                        }
                    }
                    MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), arrayOf("video/mp4"), null)
                }

                withContext(Dispatchers.Main) {
                    context.toast("Story Video saved to Gallery")
                }
            } else {
                withContext(Dispatchers.Main) {
                    context.toast("Exported high-res Story Card instead")
                }
                saveImageToGallery(context, title, artist, artworkUrl)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in saveVideoToGallery", e)
            saveImageToGallery(context, title, artist, artworkUrl)
        }
    }
}
