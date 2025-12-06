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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.day.mate.data.local.media.VaultItem
import com.day.mate.data.local.media.VaultType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VaultScreen(
    navController: NavController,
    viewModel: VaultViewModel = viewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isArabic = remember(configuration) {
        configuration.locales[0].language == "ar"
    }

    val allItems by viewModel.items.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val strings = if (isArabic) {
        VaultStrings(
            title = "مخزن الوسائط",
            subtitle = "ملفاتك مشفرة وخاصة",
            add = "إضافة",
            delete = "حذف",
            lock = "قفل",
            playAudio = "تشغيل الصوت",
            defaultFileName = "عنصر المخزن",
            filterAll = "الكل",
            filterPhotos = "الصور",
            filterVideos = "الفيديوهات",
            filterAudio = "الصوتيات",
            filterDocuments = "المستندات"
        )
    } else {
        VaultStrings(
            title = "Media Storage",
            subtitle = "Your files are encrypted and private",
            add = "Add",
            delete = "Delete",
            lock = "Lock",
            playAudio = "Play Audio",
            defaultFileName = "Vault Item",
            filterAll = "All",
            filterPhotos = "Photos",
            filterVideos = "Videos",
            filterAudio = "Audio",
            filterDocuments = "Documents"
        )
    }
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val buttonColor = if (isDarkTheme) Color(0xFFD4AF37) else MaterialTheme.colorScheme.primaryContainer
    val buttonContentColor = if (isDarkTheme) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
    LaunchedEffect(isArabic) {
        val currentFilter = selectedFilter
        val newFilter = when (currentFilter) {
            "All", "الكل" -> strings.filterAll
            "Photos", "الصور" -> strings.filterPhotos
            "Videos", "الفيديوهات" -> strings.filterVideos
            "Audio", "الصوتيات" -> strings.filterAudio
            "Documents", "المستندات" -> strings.filterDocuments
            else -> strings.filterAll
        }
        if (currentFilter != newFilter) {
            viewModel.selectFilter(newFilter)
        }
    }

    val items = when (selectedFilter) {
        strings.filterPhotos, "Photos", "الصور" -> allItems.filter { it.type == VaultType.PHOTO }
        strings.filterVideos, "Videos", "الفيديوهات" -> allItems.filter { it.type == VaultType.VIDEO }
        strings.filterAudio, "Audio", "الصوتيات" -> allItems.filter { it.type == VaultType.AUDIO }
        strings.filterDocuments, "Documents", "المستندات" -> allItems.filter { it.type == VaultType.DOCUMENT }
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

                val name = getFileName(context, uri, strings.defaultFileName)

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

                VaultItem(id = uri.hashCode(), uri = uri.toString(), type = type, name = name)
            }
            viewModel.addItems(newItems)
        }
    )

    Scaffold(
        containerColor = backgroundColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    picker.launch(arrayOf("image/*", "video/*", "audio/*", "application/pdf"))
                },
                containerColor = buttonColor,
                contentColor = buttonContentColor,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.add)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f) // تم استعادة weight(1f)
            ) {

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = strings.title,
                        color = onSurfaceColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(18.dp))
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
                                contentDescription = strings.lock,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = strings.subtitle,
                            color = onSurfaceVariantColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val filters = listOf(
                        strings.filterAll,
                        strings.filterPhotos,
                        strings.filterVideos,
                        strings.filterAudio,
                        strings.filterDocuments
                    )

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
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (selected) primaryColor
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) primaryColor
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { viewModel.selectFilter(filter) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = filter,
                                    color = if (selected) onPrimaryColor else onSurfaceColor,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                items(items) { item ->
                    VaultItemCard(
                        item = item,
                        onClick = {
                            if (item.type == VaultType.AUDIO) {
                                openAudioExternally(context, Uri.parse(item.uri), strings.playAudio)
                            } else {
                                navController.navigate("viewer/${Uri.encode(item.uri)}/${item.type.name}")
                            }
                        },
                        overlayContent = {
                            IconButton(
                                onClick = { viewModel.removeItem(item) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = strings.delete,
                                    tint = Color.White
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
data class VaultStrings(
    val title: String,
    val subtitle: String,
    val add: String,
    val delete: String,
    val lock: String,
    val playAudio: String,
    val defaultFileName: String,
    val filterAll: String,
    val filterPhotos: String,
    val filterVideos: String,
    val filterAudio: String,
    val filterDocuments: String
)

fun openAudioExternally(context: Context, uri: Uri, title: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "audio/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, title))
    } catch (e: Exception) {
        Log.e("VaultScreen", "Failed to open audio file", e)
    }
}

fun getFileName(context: Context, uri: Uri, defaultName: String): String {
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
    return result ?: uri.lastPathSegment ?: defaultName
}

@Composable
fun VaultItemCard(
    item: VaultItem,
    onClick: () -> Unit,
    overlayContent: (@Composable BoxScope.() -> Unit)? = null
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        )
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
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

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
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // عرض اسم الملف في الأسفل
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