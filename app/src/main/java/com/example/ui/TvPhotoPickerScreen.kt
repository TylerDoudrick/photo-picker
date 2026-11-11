package com.example.ui

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.model.PhotoItem
import com.example.ui.components.AlbumCard
import com.example.ui.components.PhotoThumbnailCard
import com.example.ui.components.QrCodeCard
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.GoogleBlueLight
import com.example.ui.theme.GoogleGreen
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceHighlight
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TvPhotoPickerScreen(
    viewModel: MainViewModel,
    onPhotoPicked: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val authState by viewModel.authState.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val isPickerMode by viewModel.isPickerMode.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val showQrSheet by viewModel.showQrSheet.collectAsState()
    val previewPhoto by viewModel.previewPhoto.collectAsState()

    // Remote Back button navigation
    BackHandler(enabled = true) {
        when {
            previewPhoto != null -> viewModel.closePreview()
            selectedAlbum != null -> viewModel.backToAlbums()
            showQrSheet -> viewModel.toggleQrSheet(false)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(horizontal = 36.dp, vertical = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TV App Bar
            TvTopBar(
                isPickerMode = isPickerMode,
                isConnected = authState.isConnected,
                accountEmail = authState.accountEmail,
                onToggleQr = { viewModel.toggleQrSheet(!showQrSheet) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // QR Code Connection Overlay
            AnimatedVisibility(
                visible = showQrSheet,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                QrCodeCard(
                    authState = authState,
                    onSimulateConnect = { viewModel.simulateConnect() },
                    onDisconnect = { viewModel.disconnect() },
                    onDismiss = { viewModel.toggleQrSheet(false) },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (!showQrSheet) {
                if (selectedAlbum == null) {
                    // Album Selection View
                    AlbumSelectionSection(
                        albums = albums,
                        onSelectAlbum = { viewModel.selectAlbum(it) }
                    )
                } else {
                    // Photo Grid View
                    PhotoGridSection(
                        albumTitle = selectedAlbum!!.title,
                        photoCount = selectedAlbum!!.photoCount,
                        photos = photos,
                        isPickerMode = isPickerMode,
                        onBack = { viewModel.backToAlbums() },
                        onPhotoClick = { photo ->
                            if (isPickerMode) {
                                viewModel.pickPhoto(photo) { uri ->
                                    onPhotoPicked(uri)
                                }
                            } else {
                                viewModel.openPreview(photo)
                            }
                        }
                    )
                }
            }
        }

        // Fullscreen Preview Overlay (when opened in preview mode)
        if (previewPhoto != null) {
            PhotoPreviewDialog(
                photo = previewPhoto!!,
                isPickerMode = isPickerMode,
                onDismiss = { viewModel.closePreview() },
                onSelectAsPicker = {
                    viewModel.pickPhoto(previewPhoto!!) { uri ->
                        onPhotoPicked(uri)
                    }
                },
                onSetAsWallpaper = {
                    coroutineScope.launch {
                        setSystemWallpaper(context, previewPhoto!!.highResUrl)
                    }
                }
            )
        }

        // Loading Overlay
        if (isDownloading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TvSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = GoogleBlueLight,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Preparing wallpaper for Google TV...",
                            color = TvTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvTopBar(
    isPickerMode: Boolean,
    isConnected: Boolean,
    accountEmail: String?,
    onToggleQr: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = GoogleBlueLight,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Google TV Photo Picker",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TvTextPrimary,
                        fontSize = 24.sp
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    color = if (isPickerMode) GoogleBlue.copy(alpha = 0.25f) else TvSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isPickerMode) "System Wallpaper Picker" else "Google TV Wallpaper Browser",
                        color = if (isPickerMode) GoogleBlueLight else TvTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = "Connect to Google Photos and select high-resolution TV wallpapers",
                color = TvTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 34.dp, top = 2.dp)
            )
        }

        // Account / QR Button
        Surface(
            modifier = Modifier
                .testTag("btn_top_qr_account")
                .tvFocusable(
                    shape = RoundedCornerShape(12.dp),
                    onClick = onToggleQr
                ),
            color = if (isConnected) TvSurfaceVariant else GoogleBlue.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "QR Account Setup",
                    tint = if (isConnected) GoogleGreen else GoogleBlueLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isConnected) "Photos: ${accountEmail?.substringBefore('@') ?: "Connected"}" else "Connect via Phone QR",
                    color = TvTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AlbumSelectionSection(
    albums: List<com.example.model.PhotoAlbum>,
    onSelectAlbum: (com.example.model.PhotoAlbum) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Google Photos Albums",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TvTextPrimary,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = "Select an album using your TV remote to view its wallpapers",
                    color = TvTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Row of simple albums
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(end = 36.dp, top = 8.dp, bottom = 16.dp)
        ) {
            items(albums, key = { it.id }) { album ->
                AlbumCard(
                    album = album,
                    onClick = { onSelectAlbum(album) }
                )
            }
        }
    }
}

@Composable
private fun PhotoGridSection(
    albumTitle: String,
    photoCount: Int,
    photos: List<PhotoItem>,
    isPickerMode: Boolean,
    onBack: () -> Unit,
    onPhotoClick: (PhotoItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .testTag("btn_back_to_albums")
                    .tvFocusable(
                        shape = RoundedCornerShape(10.dp),
                        onClick = onBack
                    ),
                color = TvSurfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Albums",
                        tint = TvTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Albums",
                        color = TvTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = albumTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TvTextPrimary,
                    fontSize = 20.sp
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                color = TvSurfaceHighlight,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "$photoCount photos",
                    color = TvTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (isPickerMode) "Press SELECT to pick wallpaper" else "Press SELECT to preview & apply",
                color = GoogleBlueLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4-Column Photo Grid for TV screen
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                PhotoThumbnailCard(
                    photo = photo,
                    onClick = { onPhotoClick(photo) }
                )
            }
        }
    }
}

@Composable
private fun PhotoPreviewDialog(
    photo: PhotoItem,
    isPickerMode: Boolean,
    onDismiss: () -> Unit,
    onSelectAsPicker: () -> Unit,
    onSetAsWallpaper: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fullscreen Preview Image
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                AsyncImage(
                    model = photo.highResUrl,
                    contentDescription = photo.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // Controls Bar at Bottom
            Surface(
                color = TvSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = photo.title,
                            color = TvTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "4K Ultra HD • Google Photos Wallpaper",
                            color = TvTextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onSelectAsPicker,
                            modifier = Modifier
                                .testTag("btn_confirm_pick")
                                .tvFocusable(shape = RoundedCornerShape(10.dp), onClick = onSelectAsPicker),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick as Wallpaper")
                        }

                        Button(
                            onClick = onSetAsWallpaper,
                            modifier = Modifier
                                .testTag("btn_set_system_wallpaper")
                                .tvFocusable(shape = RoundedCornerShape(10.dp), onClick = onSetAsWallpaper),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TvSurfaceHighlight)
                        ) {
                            Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set as TV Wallpaper")
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .testTag("btn_dismiss_preview")
                                .tvFocusable(shape = RoundedCornerShape(10.dp), onClick = onDismiss),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", color = TvTextPrimary)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun setSystemWallpaper(context: android.content.Context, imageUrl: String) {
    withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap

            if (bitmap != null) {
                val wallpaperManager = WallpaperManager.getInstance(context)
                wallpaperManager.setBitmap(bitmap)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Google TV Wallpaper updated!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Could not set wallpaper directly: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
