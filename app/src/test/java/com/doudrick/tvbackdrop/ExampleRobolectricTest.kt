package com.doudrick.tvbackdrop

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.doudrick.tvbackdrop.repository.GooglePhotosRepository
import com.doudrick.tvbackdrop.util.QrCodeGenerator
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
        assertEquals("TV Backdrop Photos", appName)
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

    @Test
    fun `auth starts disconnected and allows sign in and sign out`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("tv_photo_picker_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val repository = GooglePhotosRepository(context)
        // Default state must be disconnected
        val initialAuth = repository.authState.value
        org.junit.Assert.assertFalse(initialAuth.isConnected)
        assertNotNull(initialAuth.deviceAuth)
        assertTrue(initialAuth.deviceAuth!!.userCode.isNotBlank())

        // Sign in
        repository.completeDeviceAuthorization("Tyler.Doudrick@gmail.com")
        val signedInAuth = repository.authState.value
        assertTrue(signedInAuth.isConnected)
        assertEquals("Tyler.Doudrick@gmail.com", signedInAuth.accountEmail)

        // Disconnect
        repository.disconnect()
        val disconnectedAuth = repository.authState.value
        org.junit.Assert.assertFalse(disconnectedAuth.isConnected)
    }
}
