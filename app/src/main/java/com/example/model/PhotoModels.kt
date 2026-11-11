package com.example.model

data class PhotoAlbum(
    val id: String,
    val title: String,
    val coverPhotoUrl: String,
    val photoCount: Int
)

data class PhotoItem(
    val id: String,
    val albumId: String,
    val title: String,
    val thumbnailUrl: String,
    val highResUrl: String,
    val width: Int = 3840,
    val height: Int = 2160
)

data class DeviceAuthInfo(
    val deviceCode: String,
    val userCode: String,
    val verificationUrl: String,
    val expiresInSeconds: Int = 300,
    val intervalSeconds: Int = 5
)

data class AuthState(
    val isConnected: Boolean = false,
    val accountEmail: String? = null,
    val deviceAuth: DeviceAuthInfo? = null,
    val isPolling: Boolean = false
)
