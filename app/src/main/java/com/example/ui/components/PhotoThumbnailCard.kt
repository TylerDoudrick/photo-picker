package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.PhotoItem
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.tvFocusable

@Composable
fun PhotoThumbnailCard(
    photo: PhotoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(12.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .testTag("photo_card_${photo.id}")
            .tvFocusable(shape = cardShape, onClick = onClick),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = TvSurfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = photo.thumbnailUrl,
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cardShape)
            )

            // Subdued title gradient at bottom
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Text(
                    text = photo.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
