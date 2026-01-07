package com.akirachang.ai.photographer.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.renderscript.Type
import androidx.camera.core.ImageProxy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
@Suppress("DEPRECATION")
class RenderScriptYuvConverter @Inject constructor(
    @ApplicationContext context: Context
) {
    private val appContext = context.applicationContext

    companion object {
        private const val TAG = "RenderScriptYuvConverter"
    }

    private var rs: RenderScript? = null
    private var script: ScriptIntrinsicYuvToRGB? = null

    private var yuvType: Type? = null
    private var yuvAllocation: Allocation? = null
    private var rgbType: Type? = null
    private var outAllocation: Allocation? = null

    private var nv21: ByteArray = ByteArray(0)
    private var yRow: ByteArray = ByteArray(0)
    private var uRow: ByteArray = ByteArray(0)
    private var vRow: ByteArray = ByteArray(0)

    private var lastWidth = 0
    private var lastHeight = 0

    fun isInitialized(): Boolean = rs != null && script != null

    fun initIfNeeded() {
        if (isInitialized()) {
            Log.d(TAG, "[v2.8.2] Already initialized")
            return
        }
        Log.d(TAG, "[v2.8.2] Initializing RenderScript...")
        try {
            rs = RenderScript.create(appContext)
            Log.d(TAG, "[v2.8.2] RenderScript.create() success: $rs")
            script = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))
            Log.d(TAG, "[v2.8.2] ScriptIntrinsicYuvToRGB.create() success: $script")
        } catch (e: Exception) {
            Log.e(TAG, "[v2.8.2] RenderScript initialization failed", e)
            throw e
        }
    }

    fun convert(image: ImageProxy, outBitmap: Bitmap) {
        require(image.format == ImageFormat.YUV_420_888) { "Unsupported ImageProxy format=${image.format}" }
        require(outBitmap.config == Bitmap.Config.ARGB_8888) { "outBitmap must be ARGB_8888" }
        require(outBitmap.width == image.width && outBitmap.height == image.height) {
            "outBitmap size ${outBitmap.width}x${outBitmap.height} != image ${image.width}x${image.height}"
        }

        initIfNeeded()

        val width = image.width
        val height = image.height

        ensureBuffers(width, height, image)
        imageToNv21(image, nv21)

        if (width != lastWidth || height != lastHeight || yuvAllocation == null || outAllocation == null) {
            destroyAllocations()

            val rs = checkNotNull(rs)

            yuvType = Type.Builder(rs, Element.U8(rs))
                .setX(width)
                .setY(height)
                .setYuvFormat(ImageFormat.NV21)
                .create()
            yuvAllocation = Allocation.createTyped(rs, yuvType, Allocation.USAGE_SCRIPT)

            rgbType = Type.Builder(rs, Element.RGBA_8888(rs))
                .setX(width)
                .setY(height)
                .create()
            outAllocation = Allocation.createTyped(rs, rgbType, Allocation.USAGE_SCRIPT)

            lastWidth = width
            lastHeight = height
        }

        val yuvAllocation = checkNotNull(yuvAllocation)
        val outAllocation = checkNotNull(outAllocation)
        val script = checkNotNull(script)

        yuvAllocation.copyFrom(nv21)
        script.setInput(yuvAllocation)
        script.forEach(outAllocation)
        outAllocation.copyTo(outBitmap)
    }

    fun destroy() {
        destroyAllocations()
        script?.destroy()
        rs?.destroy()
        script = null
        rs = null
        lastWidth = 0
        lastHeight = 0
    }

    private fun destroyAllocations() {
        yuvAllocation?.destroy()
        outAllocation?.destroy()
        yuvType = null
        rgbType = null
        yuvAllocation = null
        outAllocation = null
    }

    private fun ensureBuffers(width: Int, height: Int, image: ImageProxy) {
        val nv21Size = width * height * 3 / 2
        if (nv21.size != nv21Size) {
            nv21 = ByteArray(nv21Size)
        }

        val yRowStride = image.planes[0].rowStride
        val uRowStride = image.planes[1].rowStride
        val vRowStride = image.planes[2].rowStride

        if (yRow.size < yRowStride) yRow = ByteArray(yRowStride)
        if (uRow.size < uRowStride) uRow = ByteArray(uRowStride)
        if (vRow.size < vRowStride) vRow = ByteArray(vRowStride)
    }

    private fun imageToNv21(image: ImageProxy, out: ByteArray) {
        val width = image.width
        val height = image.height

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer.duplicate()
        val uBuffer = uPlane.buffer.duplicate()
        val vBuffer = vPlane.buffer.duplicate()

        yBuffer.rewind()
        uBuffer.rewind()
        vBuffer.rewind()

        val yRowStride = yPlane.rowStride
        val ySize = width * height

        var outOffset = 0
        if (yRowStride == width) {
            yBuffer.get(out, 0, ySize)
            outOffset = ySize
        } else {
            for (row in 0 until height) {
                yBuffer.position(row * yRowStride)
                yBuffer.get(out, outOffset, width)
                outOffset += width
            }
        }

        val chromaHeight = height / 2
        val chromaWidth = width / 2

        val uRowStride = uPlane.rowStride
        val vRowStride = vPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vPixelStride = vPlane.pixelStride

        if (uPixelStride == 2 && vPixelStride == 2 && uRowStride == vRowStride) {
            val v0 = vBuffer.get(0)
            val u0 = uBuffer.get(0)
            val v1 = vBuffer.get(1)

            if (u0 == v1) {
                // NV21 fast path: V buffer contains interleaved VU data
                // Each chroma row should have exactly 'width' bytes (chromaWidth * 2)
                val bytesPerRow = chromaWidth * 2  // VU pairs
                if (vRowStride < bytesPerRow) {
                    Log.w(TAG, "[v2.8.2] NV21 fast path failed: vRowStride ($vRowStride) < bytesPerRow ($bytesPerRow)")
                    // Fall through to slow path
                    uBuffer.rewind()
                    vBuffer.rewind()
                } else {
                    Log.d(TAG, "[v2.8.2] NV21 fast path: chromaHeight=$chromaHeight, chromaWidth=$chromaWidth, vRowStride=$vRowStride, bytesPerRow=$bytesPerRow, vBuffer.capacity=${vBuffer.capacity()}")
                    for (row in 0 until chromaHeight) {
                        val targetPos = row * vRowStride
                        vBuffer.position(targetPos)
                        val remaining = vBuffer.remaining()
                        // Last row might have fewer bytes available
                        val actualBytes = minOf(bytesPerRow, remaining)
                        vBuffer.get(out, outOffset, actualBytes)
                        outOffset += actualBytes
                    }
                    return
                }
            } else {
                // Reset if probe advanced positions
                uBuffer.rewind()
                vBuffer.rewind()
            }
        }

        for (row in 0 until chromaHeight) {
            uBuffer.position(row * uRowStride)
            uBuffer.get(uRow, 0, uRowStride)

            vBuffer.position(row * vRowStride)
            vBuffer.get(vRow, 0, vRowStride)

            var uColOffset = 0
            var vColOffset = 0
            for (col in 0 until chromaWidth) {
                out[outOffset++] = vRow[vColOffset]
                out[outOffset++] = uRow[uColOffset]
                uColOffset += uPixelStride
                vColOffset += vPixelStride
            }
        }
    }
}
