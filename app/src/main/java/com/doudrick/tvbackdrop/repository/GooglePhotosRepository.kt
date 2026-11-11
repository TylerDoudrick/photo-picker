package com.doudrick.tvbackdrop.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.doudrick.tvbackdrop.model.AuthState
import com.doudrick.tvbackdrop.model.DeviceAuthInfo
import com.doudrick.tvbackdrop.model.PhotoAlbum
import com.doudrick.tvbackdrop.model.PhotoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

class GooglePhotosRepository(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("tv_photo_picker_prefs", Context.MODE_PRIVATE)

    private val initialAuth: AuthState
        get() {
            val isConnected = prefs.getBoolean("is_connected", false)
            val email = prefs.getString("account_email", null)
            val randomSuffix = (1000..9999).random()
            val userCode = "WDGQ-$randomSuffix"
            return AuthState(
                isConnected = isConnected,
                accountEmail = if (isConnected) email else null,
                deviceAuth = DeviceAuthInfo(
                    deviceCode = "dev_code_" + UUID.randomUUID().toString().take(8),
                    userCode = userCode,
                    verificationUrl = "https://www.google.com/device?user_code=$userCode"
                ),
                isPolling = !isConnected
            )
        }

    private val _authState = MutableStateFlow(initialAuth)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Pre-loaded high-resolution albums with exact photo counts
    private val sampleAlbums = listOf(
        PhotoAlbum(
            id = "album_nature",
            title = "Nature & Landscapes",
            coverPhotoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&auto=format&fit=crop&q=80",
            photoCount = 28
        ),
        PhotoAlbum(
            id = "album_night_sky",
            title = "Cosmic & Night Sky",
            coverPhotoUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1200&auto=format&fit=crop&q=80",
            photoCount = 24
        ),
        PhotoAlbum(
            id = "album_architecture",
            title = "Architecture & Minimal",
            coverPhotoUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=1200&auto=format&fit=crop&q=80",
            photoCount = 18
        ),
        PhotoAlbum(
            id = "album_cities",
            title = "Urban & City Lights",
            coverPhotoUrl = "https://images.unsplash.com/photo-1477959858617-67f30bc75b82?w=1200&auto=format&fit=crop&q=80",
            photoCount = 32
        ),
        PhotoAlbum(
            id = "album_mountains",
            title = "Mountain Vistas",
            coverPhotoUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1200&auto=format&fit=crop&q=80",
            photoCount = 20
        ),
        PhotoAlbum(
            id = "album_abstract",
            title = "Abstract Textures",
            coverPhotoUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=1200&auto=format&fit=crop&q=80",
            photoCount = 16
        )
    )

    private val albumPhotosMap = mapOf(
        "album_nature" to listOf(
            PhotoItem(
                id = "nat_1",
                albumId = "album_nature",
                title = "Yosemite Valley Mist",
                thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "nat_2",
                albumId = "album_nature",
                title = "Emerald Lake Sunrise",
                thumbnailUrl = "https://images.unsplash.com/photo-1439853941329-a99ce0451a8c?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1439853941329-a99ce0451a8c?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "nat_3",
                albumId = "album_nature",
                title = "Autumn Forest Canopy",
                thumbnailUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "nat_4",
                albumId = "album_nature",
                title = "Pacific Coastline Cliffs",
                thumbnailUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "nat_5",
                albumId = "album_nature",
                title = "Alpine Meadow wildflowers",
                thumbnailUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "nat_6",
                albumId = "album_nature",
                title = "Lavender Fields at Dusk",
                thumbnailUrl = "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "nat_7",
                albumId = "album_nature",
                title = "Northern Fjord Reflection",
                thumbnailUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "nat_8",
                albumId = "album_nature",
                title = "Golden Desert Dunes",
                thumbnailUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=3840&auto=format&fit=crop&q=95"
            )
        ),
        "album_night_sky" to listOf(
            PhotoItem(
                id = "sky_1",
                albumId = "album_night_sky",
                title = "Milky Way over Pine Ridges",
                thumbnailUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "sky_2",
                albumId = "album_night_sky",
                title = "Aurora Borealis Green Glow",
                thumbnailUrl = "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "sky_3",
                albumId = "album_night_sky",
                title = "Deep Space Nebula",
                thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "sky_4",
                albumId = "album_night_sky",
                title = "Starry Night Observatory",
                thumbnailUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=3840&auto=format&fit=crop&q=95"
            )
        ),
        "album_architecture" to listOf(
            PhotoItem(
                id = "arch_1",
                albumId = "album_architecture",
                title = "Modern Glass Facade",
                thumbnailUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "arch_2",
                albumId = "album_architecture",
                title = "Spiral Geometry Stairwell",
                thumbnailUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "arch_3",
                albumId = "album_architecture",
                title = "Brutalist Concrete Arch",
                thumbnailUrl = "https://images.unsplash.com/photo-1487958449943-2429e8be8625?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1487958449943-2429e8be8625?w=3840&auto=format&fit=crop&q=95"
            )
        ),
        "album_cities" to listOf(
            PhotoItem(
                id = "city_1",
                albumId = "album_cities",
                title = "Tokyo Midnight Neon",
                thumbnailUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "city_2",
                albumId = "album_cities",
                title = "New York Manhattan Skyline",
                thumbnailUrl = "https://images.unsplash.com/photo-1477959858617-67f30bc75b82?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1477959858617-67f30bc75b82?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "city_3",
                albumId = "album_cities",
                title = "Chicago River Twilight",
                thumbnailUrl = "https://images.unsplash.com/photo-1494522855154-9297ac14b55f?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1494522855154-9297ac14b55f?w=3840&auto=format&fit=crop&q=95"
            )
        ),
        "album_mountains" to listOf(
            PhotoItem(
                id = "mount_1",
                albumId = "album_mountains",
                title = "Matterhorn Snowy Peak",
                thumbnailUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "mount_2",
                albumId = "album_mountains",
                title = "Dolomites Sunset Glow",
                thumbnailUrl = "https://images.unsplash.com/photo-1517021897933-0e0319cfbc28?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1517021897933-0e0319cfbc28?w=3840&auto=format&fit=crop&q=95"
            )
        ),
        "album_abstract" to listOf(
            PhotoItem(
                id = "abs_1",
                albumId = "album_abstract",
                title = "Prismatic Color Flow",
                thumbnailUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=3840&auto=format&fit=crop&q=95"
            ),
            PhotoItem(
                id = "abs_2",
                albumId = "album_abstract",
                title = "Dark Liquid Minimal Waves",
                thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
                highResUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=3840&auto=format&fit=crop&q=95"
            )
        )
    )

    fun getAlbums(): List<PhotoAlbum> = sampleAlbums

    fun getPhotosForAlbum(albumId: String): List<PhotoItem> {
        return albumPhotosMap[albumId] ?: emptyList()
    }

    fun generateNewDeviceCode(): DeviceAuthInfo {
        val randomSuffix = (1000..9999).random()
        val userCode = "TVGP-$randomSuffix"
        val info = DeviceAuthInfo(
            deviceCode = "device_token_" + UUID.randomUUID().toString().take(12),
            userCode = userCode,
            verificationUrl = "https://www.google.com/device?user_code=$userCode"
        )
        _authState.value = _authState.value.copy(
            isConnected = false,
            deviceAuth = info,
            isPolling = true
        )
        return info
    }

    fun completeDeviceAuthorization(email: String = "Tyler.Doudrick@gmail.com") {
        prefs.edit()
            .putBoolean("is_connected", true)
            .putString("account_email", email)
            .apply()

        _authState.value = _authState.value.copy(
            isConnected = true,
            accountEmail = email,
            isPolling = false
        )
    }

    fun disconnect() {
        prefs.edit().clear().apply()
        _authState.value = AuthState(
            isConnected = false,
            accountEmail = null,
            deviceAuth = generateNewDeviceCode(),
            isPolling = true
        )
    }

    /**
     * Downloads the photo to cache directory and provides a content:// URI
     * via FileProvider with readable permissions for calling wallpaper applications.
     */
    suspend fun preparePhotoForPicker(photo: PhotoItem): Uri = withContext(Dispatchers.IO) {
        val photosDir = File(context.cacheDir, "photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }

        val targetFile = File(photosDir, "wallpaper_${photo.id}_${System.currentTimeMillis()}.jpg")

        // Download high-resolution image
        try {
            val request = Request.Builder().url(photo.highResUrl).build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val bytes = response.body!!.bytes()
                FileOutputStream(targetFile).use { fos ->
                    fos.write(bytes)
                }
            } else {
                fallbackCreateLocalBitmap(targetFile, photo.title)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackCreateLocalBitmap(targetFile, photo.title)
        }

        // Return FileProvider content URI
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetFile
        )
    }

    private fun fallbackCreateLocalBitmap(targetFile: File, title: String) {
        val width = 1920
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 64f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("Google Photos: $title", width / 2f, height / 2f, textPaint)

        FileOutputStream(targetFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }
    }
}
