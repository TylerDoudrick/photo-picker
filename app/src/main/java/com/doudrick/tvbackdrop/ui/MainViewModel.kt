package com.doudrick.tvbackdrop.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.doudrick.tvbackdrop.model.AuthState
import com.doudrick.tvbackdrop.model.PhotoAlbum
import com.doudrick.tvbackdrop.model.PhotoItem
import com.doudrick.tvbackdrop.repository.GooglePhotosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GooglePhotosRepository(application)

    val authState: StateFlow<AuthState> = repository.authState

    private val _albums = MutableStateFlow<List<PhotoAlbum>>(emptyList())
    val albums: StateFlow<List<PhotoAlbum>> = _albums.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<PhotoAlbum?>(null)
    val selectedAlbum: StateFlow<PhotoAlbum?> = _selectedAlbum.asStateFlow()

    private val _photos = MutableStateFlow<List<PhotoItem>>(emptyList())
    val photos: StateFlow<List<PhotoItem>> = _photos.asStateFlow()

    private val _previewPhoto = MutableStateFlow<PhotoItem?>(null)
    val previewPhoto: StateFlow<PhotoItem?> = _previewPhoto.asStateFlow()

    private val _isPickerMode = MutableStateFlow(false)
    val isPickerMode: StateFlow<Boolean> = _isPickerMode.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _showQrSheet = MutableStateFlow(false)
    val showQrSheet: StateFlow<Boolean> = _showQrSheet.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        loadAlbums()
    }

    fun setPickerMode(isPicker: Boolean) {
        _isPickerMode.value = isPicker
    }

    private fun loadAlbums() {
        val loadedAlbums = repository.getAlbums()
        _albums.value = loadedAlbums
    }

    fun selectAlbum(album: PhotoAlbum) {
        _selectedAlbum.value = album
        _photos.value = repository.getPhotosForAlbum(album.id)
    }

    fun backToAlbums() {
        _selectedAlbum.value = null
        _photos.value = emptyList()
        _previewPhoto.value = null
    }

    fun toggleQrSheet(show: Boolean) {
        _showQrSheet.value = show
    }

    fun signInWithGoogle(email: String = "Tyler.Doudrick@gmail.com") {
        repository.completeDeviceAuthorization(email)
        _isGuestMode.value = false
        _showQrSheet.value = false
        _statusMessage.value = "Connected as $email"
        loadAlbums()
    }

    fun simulateConnect() {
        signInWithGoogle("Tyler.Doudrick@gmail.com")
    }

    fun refreshPairingCode() {
        repository.generateNewDeviceCode()
        _statusMessage.value = "New pairing code generated"
    }

    fun enterGuestMode() {
        _isGuestMode.value = true
        loadAlbums()
    }

    fun exitGuestMode() {
        _isGuestMode.value = false
        backToAlbums()
    }

    fun disconnect() {
        repository.disconnect()
        _isGuestMode.value = false
        _showQrSheet.value = false
        backToAlbums()
        _statusMessage.value = "Disconnected from Google Photos"
    }

    fun openPreview(photo: PhotoItem) {
        _previewPhoto.value = photo
    }

    fun closePreview() {
        _previewPhoto.value = null
    }

    fun pickPhoto(photo: PhotoItem, onUriReady: (Uri) -> Unit) {
        viewModelScope.launch {
            _isDownloading.value = true
            _statusMessage.value = "Preparing wallpaper image..."
            try {
                val uri = repository.preparePhotoForPicker(photo)
                _statusMessage.value = "Wallpaper ready!"
                onUriReady(uri)
            } catch (e: Exception) {
                _statusMessage.value = "Failed to prepare photo: ${e.localizedMessage}"
            } finally {
                _isDownloading.value = false
            }
        }
    }
}
