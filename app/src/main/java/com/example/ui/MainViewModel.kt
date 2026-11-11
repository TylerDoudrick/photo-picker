package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AuthState
import com.example.model.PhotoAlbum
import com.example.model.PhotoItem
import com.example.repository.GooglePhotosRepository
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

    fun simulateConnect() {
        repository.completeDeviceAuthorization("tyler.photos@gmail.com")
        _statusMessage.value = "Connected to Google Photos"
        _showQrSheet.value = false
    }

    fun disconnect() {
        repository.disconnect()
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
