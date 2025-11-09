package com.day.mate.ui.theme.screens.media

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
// ✅ استيراد مُعدّلات الإيماءات
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
// ✅ استيراد graphicsLayer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.day.mate.data.local.VaultType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VaultViewerScreen(
    navController: NavController,
    uri: String,
    type: String
) {
    val context = LocalContext.current
    val vaultType = remember { VaultType.valueOf(type) }
    val uriParsed = remember { Uri.parse(uri) }

    LaunchedEffect(uri) {
        Log.d("VaultViewer", "Received URI: $uri, Type: $type")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            when (vaultType) {

                VaultType.PHOTO -> {
                    // 🚨 متغيرات الحالة للتكبير والإزاحة
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    // 🚨 معالج الإيماءات (TransformableState)
                    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                        scale = (scale * zoomChange).coerceIn(1f, 5f) // الحد الأدنى 1x والأقصى 5x

                        // تطبيق الإزاحة بعد التكبير
                        val newOffset = offset + offsetChange * scale
                        // يمكنك إضافة قيود لمنع سحب الصورة خارج حدود الرؤية هنا
                        offset = newOffset
                    }

                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            // ✅ تطبيق مُعدّل transformable لتلقي الإيماءات
                            .transformable(state = state)
                            // ✅ تطبيق graphicsLayer لتحويل الصورة
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentScale = ContentScale.Fit
                    )
                }

                VaultType.VIDEO -> {
                    val exoPlayer = remember {
                        ExoPlayer.Builder(context).build().apply {
                            val mediaItem = MediaItem.fromUri(uriParsed)
                            setMediaItem(mediaItem)

                            addListener(object : Player.Listener {
                                override fun onPlayerError(error: PlaybackException) {
                                    Log.e("EXO_ERROR", "Playback Error for URI: $uri", error)
                                    Toast.makeText(context, "فشل تشغيل الفيديو: ${error.errorCodeName}", Toast.LENGTH_LONG).show()
                                }
                            })

                            prepare()
                            playWhenReady = true
                        }
                    }

                    DisposableEffect(key1 = Unit) {
                        onDispose {
                            exoPlayer.release()
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = true
                                player = exoPlayer
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                VaultType.DOCUMENT -> {
                    LaunchedEffect(uriParsed) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(uriParsed, "application/pdf")
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                                navController.popBackStack()
                            } else {
                                Log.w("PDF_WARN", "No PDF viewer application found on device.")
                                Toast.makeText(context, "لا يوجد تطبيق لفتح ملفات PDF", Toast.LENGTH_LONG).show()
                            }

                        } catch (e: Exception) {
                            Log.e("PDF_ERROR", "Error opening PDF URI: $uri", e)
                            Toast.makeText(context, "فشل فتح المستند: خطأ في الوصول", Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Opening Document...", color = Color.White)
                    }
                }
            }
        }
    }
}