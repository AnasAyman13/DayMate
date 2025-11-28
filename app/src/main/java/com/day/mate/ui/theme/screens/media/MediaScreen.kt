package com.day.mate.ui.theme.screens.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.day.mate.data.local.VaultItem
import com.day.mate.data.local.VaultType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VaultScreen(
    navController: NavController,
    viewModel: VaultViewModel = viewModel()
) {
    val context = LocalContext.current

    val allItems by viewModel.items.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    val items = when (selectedFilter) {
        "Photos" -> allItems.filter { it.type == VaultType.PHOTO }
        "Videos" -> allItems.filter { it.type == VaultType.VIDEO }
        "Audio" -> allItems.filter { it.type == VaultType.AUDIO }
        "Documents" -> allItems.filter { it.type == VaultType.DOCUMENT }
        else -> allItems
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            val newItems = uris.mapNotNull { uri ->

                val mimeType = context.contentResolver.getType(uri)

                val type = when {
                    mimeType?.startsWith("image/") == true -> VaultType.PHOTO
                    mimeType?.startsWith("video/") == true -> VaultType.VIDEO
                    mimeType?.startsWith("audio/") == true -> VaultType.AUDIO
                    mimeType == "application/pdf" -> VaultType.DOCUMENT
                    else -> {
                        Log.e("VaultScreen", "Unknown or unsupported MIME type ($mimeType) for URI: $uri")
                        return@mapNotNull null
                    }
                }

                // ✅ استخراج اسم الملف
                val name = getFileName(context, uri)

                // منح الأذونات المستمرة
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    Log.d("VAULT_PERMISSION", "Permission granted for URI: $uri")
                } catch (e: Exception) {
                    Log.e("VAULT_PERMISSION", "FAILED to grant persistable permission for URI: $uri", e)
                    e.printStackTrace()
                }

                // ✅ تمرير اسم الملف إلى VaultItem
                VaultItem(id = uri.hashCode(), uri = uri.toString(), type = type, name = name)
            }
            viewModel.addItems(newItems)
        }
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // ✅ إضافة audio/* للأنواع المدعومة
                    picker.launch(arrayOf("image/*", "video/*", "audio/*", "application/pdf"))
                },
                containerColor = Color(0xFFFFD700),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101F22))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // العنوان أعلى يسار
                Text(
                    text = "Media Storage",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                )

                Spacer(Modifier.height(18.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF81D4FA), Color(0xFF4DB6AC))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Your files are encrypted and private.",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // ✅ إضافة "Audio" للفلاتر
                val filters = listOf("All", "Photos", "Videos", "Audio", "Documents")
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        val selected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (selected) Color(0xFF4DB6AC)
                                    else Color.White.copy(alpha = 0.1f)
                                )
                                .clickable { viewModel.selectFilter(filter) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (selected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items) { item ->
                        VaultItemCard(
                            item = item,
                            onClick = {
                                // ✅ للملفات الصوتية نفتحها خارج التطبيق مباشرة
                                if (item.type == VaultType.AUDIO) {
                                    openAudioExternally(context, Uri.parse(item.uri))
                                } else {
                                    navController.navigate("viewer/${Uri.encode(item.uri)}/${item.type.name}")
                                }
                            },
                            overlayContent = {
                                IconButton(
                                    onClick = { viewModel.removeItem(item) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ✅ دالة لفتح الملفات الصوتية خارج التطبيق
fun openAudioExternally(context: Context, uri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "audio/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Play Audio"))
    } catch (e: Exception) {
        Log.e("VaultScreen", "Failed to open audio file", e)
    }
}

// دالة مساعدة للحصول على اسم الملف
fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    result = it.getString(displayNameIndex)
                }
            }
        }
    }
    return result ?: uri.lastPathSegment ?: "Vault Item"
}

@Composable
fun VaultItemCard(
    item: VaultItem,
    onClick: () -> Unit,
    overlayContent: (@Composable BoxScope.() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (item.type) {
                VaultType.PHOTO -> AsyncImage(
                    model = item.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                VaultType.VIDEO -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }

                // ✅ إضافة كارت للصوتيات
                VaultType.AUDIO -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF6A1B9A), Color(0xFFAB47BC))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }

                VaultType.DOCUMENT -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                }
            }

            // ✅ إضافة الـOverlay في الأسفل لعرض اسم الملف
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            overlayContent?.let { it() }
        }
    }
}