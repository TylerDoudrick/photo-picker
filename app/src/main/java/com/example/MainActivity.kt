package com.example

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.TvPhotoPickerScreen
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvPhotoPickerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val action = intent?.action
        val isPickerMode = action == Intent.ACTION_GET_CONTENT || action == Intent.ACTION_PICK
        viewModel.setPickerMode(isPickerMode)

        setContent {
            TvPhotoPickerTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = TvBackground
                ) {
                    TvPhotoPickerScreen(
                        viewModel = viewModel,
                        onPhotoPicked = { uri ->
                            handlePhotoPicked(uri)
                        }
                    )
                }
            }
        }
    }

    private fun handlePhotoPicked(uri: Uri) {
        val resultIntent = Intent().apply {
            data = uri
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
