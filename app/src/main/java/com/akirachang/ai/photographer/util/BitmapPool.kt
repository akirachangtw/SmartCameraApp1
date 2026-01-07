package com.akirachang.ai.photographer.util

import android.graphics.Bitmap
import android.util.Log

class BitmapPool(
    private val maxEntriesPerKey: Int = 4
) {
    data class BitmapBuffer(
        val bitmap: Bitmap,
        val pixels: IntArray
    )

    private data class Key(
        val width: Int,
        val height: Int,
        val config: Bitmap.Config
    )

    private val pools = mutableMapOf<Key, ArrayDeque<BitmapBuffer>>()

    // v2.8.1 Phase A1: Hit/Miss tracking
    private var hits = 0
    private var misses = 0

    // v2.8.2 Phase D: Enhanced statistics
    private var createdCount = 0
    private val keyMisses = mutableMapOf<Key, Int>()

    companion object {
        private const val TAG = "BitmapPool"
    }

    fun acquire(
        width: Int,
        height: Int,
        config: Bitmap.Config = Bitmap.Config.ARGB_8888
    ): BitmapBuffer {
        val key = Key(width = width, height = height, config = config)
        val deque = pools.getOrPut(key) { ArrayDeque() }
        val buffer = deque.removeFirstOrNull()

        // v2.8.1 Phase A1: Track hit/miss
        if (buffer != null) {
            hits++
            val total = hits + misses
            if (total % 100 == 0) {
                val hitRate = (hits * 100) / total
                Log.v(TAG, "[v2.8.1 Pool] Hit rate: $hitRate% ($hits/$total), CurrentPoolSize: ${getCurrentPoolSize()}")
            }
            return buffer
        } else {
            misses++
            createdCount++
            keyMisses[key] = keyMisses.getOrDefault(key, 0) + 1

            // v2.8.2 Phase D: Log key statistics every 30 misses
            if (misses % 30 == 0) {
                logDetailedStats()
            }

            return BitmapBuffer(
                bitmap = Bitmap.createBitmap(width, height, config),
                pixels = IntArray(width * height)
            )
        }
    }

    fun release(buffer: BitmapBuffer) {
        if (buffer.bitmap.isRecycled) return

        val key = Key(
            width = buffer.bitmap.width,
            height = buffer.bitmap.height,
            config = buffer.bitmap.config ?: Bitmap.Config.ARGB_8888
        )

        val deque = pools.getOrPut(key) { ArrayDeque() }
        if (deque.size >= maxEntriesPerKey) return
        deque.addLast(buffer)
    }

    fun clear() {
        pools.clear()
    }

    // v2.8.1 Phase A1: Get current pool size for diagnostics
    fun getCurrentPoolSize(): Int {
        return pools.values.sumOf { it.size }
    }

    // v2.8.2 Phase D: Detailed statistics logging
    private fun logDetailedStats() {
        val keyCount = pools.keys.size
        val totalBitmaps = pools.values.sumOf { it.size }
        val total = hits + misses
        val hitRate = if (total > 0) (hits * 100) / total else 0

        Log.d(TAG, "[v2.8.2 Pool Stats] Keys=$keyCount, Bitmaps=$totalBitmaps, " +
                "Hit=${hitRate}% ($hits/$total), Created=$createdCount")

        // Log top 3 keys by miss count
        keyMisses.entries
            .sortedByDescending { it.value }
            .take(3)
            .forEachIndexed { idx, entry ->
                val k = entry.key
                val missCount = entry.value
                Log.d(TAG, "[v2.8.2 Pool Key#${idx+1}] ${k.width}x${k.height} " +
                        "(${k.config}) misses=$missCount")
            }
    }

    // v2.8.2 Phase D: Get key statistics for external monitoring
    fun getKeyCount(): Int = pools.keys.size
    fun getCreatedCount(): Int = createdCount
}
