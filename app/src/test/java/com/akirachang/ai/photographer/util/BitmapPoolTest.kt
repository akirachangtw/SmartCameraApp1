package com.akirachang.ai.photographer.util

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BitmapPoolTest {

    @Test
    fun acquire_release_acquire_reusesBuffer() {
        val pool = BitmapPool(maxEntriesPerKey = 2)

        val buffer1 = pool.acquire(width = 8, height = 6, config = Bitmap.Config.ARGB_8888)
        assertEquals(8, buffer1.bitmap.width)
        assertEquals(6, buffer1.bitmap.height)
        assertEquals(8 * 6, buffer1.pixels.size)

        pool.release(buffer1)

        val buffer2 = pool.acquire(width = 8, height = 6, config = Bitmap.Config.ARGB_8888)
        assertSame(buffer1.bitmap, buffer2.bitmap)
        assertSame(buffer1.pixels, buffer2.pixels)
    }

    @Test
    fun release_respectsMaxEntriesPerKey() {
        val pool = BitmapPool(maxEntriesPerKey = 1)

        val a = pool.acquire(width = 4, height = 4, config = Bitmap.Config.ARGB_8888)
        val b = pool.acquire(width = 4, height = 4, config = Bitmap.Config.ARGB_8888)
        pool.release(a)
        pool.release(b)

        val c = pool.acquire(width = 4, height = 4, config = Bitmap.Config.ARGB_8888)
        val d = pool.acquire(width = 4, height = 4, config = Bitmap.Config.ARGB_8888)

        assertSame(a.bitmap, c.bitmap)
        assertNotSame(b.bitmap, d.bitmap)
    }
}

