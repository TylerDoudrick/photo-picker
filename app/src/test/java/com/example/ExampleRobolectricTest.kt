package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.repository.GooglePhotosRepository
import com.example.util.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TV Photo Picker", appName)
    }

    @Test
    fun `albums have titles and photo counts`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = GooglePhotosRepository(context)
        val albums = repository.getAlbums()

        assertTrue(albums.isNotEmpty())
        albums.forEach { album ->
            assertTrue(album.title.isNotBlank())
            assertTrue(album.photoCount > 0)
        }
    }

    @Test
    fun `qr code generator creates bitmap for tv code`() {
        val bitmap = QrCodeGenerator.generateQrBitmap("https://www.google.com/device?user_code=TVGP-4819", 256)
        assertNotNull(bitmap)
    }
}
